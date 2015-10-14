package sk.codekitchen.smartfuel.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.skyscreamer.jsonassert.JSONAssert;

import java.text.ParseException;
import java.util.HashSet;

import sk.codekitchen.smartfuel.BuildConfig;
import sk.codekitchen.smartfuel.exception.DuplicateSavepointException;
import sk.codekitchen.smartfuel.exception.UnknownUserException;
import sk.codekitchen.smartfuel.util.GLOBALS;
import sk.codekitchen.smartfuel.util.ServerAPI;

/**
 *
 * @author Attila Večerek
 */
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricGradleTestRunner.class)
public class TestSFDB extends AndroidTestCase {

	public static final String LOG_TAG = TestSFDB.class.getSimpleName();
	protected static final String SETUP_SAVEPOINT = "SETUP";
	protected static final String INSERT_SAVEPOINT = "INSERT";

	protected SFDB sfdb;
	protected SharedPreferences prefs;

	protected JSONObject snapshots = new JSONObject();

	/*
		Sets up the testing environment before each test.
	 */
	@Before
	public void setUp()
			throws DuplicateSavepointException, ParseException, UnknownUserException {
		Context context = RuntimeEnvironment.application.getApplicationContext();
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (prefs.getInt(GLOBALS.USER_ID, -1) != 1) {
			prefs.edit().putInt(GLOBALS.USER_ID, 1).commit();
		}

		if (sfdb == null) {
			sfdb = new SFDB(context);
			sfdb.savepoint(SETUP_SAVEPOINT);
		} else {
			sfdb.savepoint();
		}
	}

	/*
		Rolls back all the alterations made during the test cases.
	 */
	/*public void tearDown() {
		if (sfdb != null) {
			sfdb.rollbackTo(SETUP_SAVEPOINT);
		}
		prefs.edit().clear().commit();
	}*/

	@Test
	public void testCreateDb() throws Throwable {
		final HashSet<String> tableNameHS = new HashSet<>();
		tableNameHS.add(User.TABLE.NAME);
		tableNameHS.add(Statistics.VIEW.NAME);
		tableNameHS.add(SmartFuelActivity.TABLE.NAME);
		tableNameHS.add(Event.TABLE.NAME);
		tableNameHS.add(Event.CONTENT.TABLE.NAME);

		//database connection OK?
		assertEquals(true, sfdb.db.isOpen());

		Cursor c = sfdb.db
				.rawQuery("SELECT name FROM sqlite_master WHERE type='table' OR type='view'", null);

		//tables created well?
		assertTrue("Error: Database not created correctly", c.moveToFirst());

		do {
			tableNameHS.remove(c.getString(0));
		} while (c.moveToNext());

		c.close();

		//no missing tables?
		assertTrue("Error: Database is missing tables", tableNameHS.isEmpty());

		testUserTableStructure();
		testRoadActivityTableStructure();
		testEventTableStructure();
		testEventContentTableStructure();
		testStatsViewStructure();
		testIndexExistence(Event.CONTENT.TABLE.COLUMN.EVENT_ID + "_index");
	}

	@Test
	public void testSyncDB() throws Throwable {
		String lastUpdate = prefs.getString(GLOBALS.LAST_UPDATE, "");
		sfdb.sync();

		assertFalse("Error: Last update time has not changed", lastUpdate.equals(prefs.getString(GLOBALS.LAST_UPDATE, "")));

		String userID = String.valueOf(prefs.getInt(GLOBALS.USER_ID, 1));
		JSONObject localUserData = sfdb.queryUserData();
		JSONObject serverUserData = new ServerAPI("test/user_data/" + userID).sendRequest();

		//Remove local DB specific columns
		localUserData.remove(User.TABLE.COLUMN.EDITED);

		//Are the local and server user data the same after synchronization?
		JSONAssert.assertEquals(localUserData, serverUserData, false);

		JSONArray localActivities = sfdb.queryActivities();
		JSONArray serverActivities =
				new ServerAPI("test/road_activities/" + userID)
						.sendRequest().getJSONArray("data");

		//Are the local and server activity records the same after synchronization?
		assertTrue("Error: the user activities records do not match",
				localActivities.toString().equals(serverActivities.toString()));

		JSONObject localUserStats = sfdb.queryStats();
		JSONObject serverUserStats = new ServerAPI("test/statistics/" + userID).sendRequest();

		//Are the local and server user statistics the same after synchronization?
		assertTrue("Error: the user statistics do not match",
				localUserStats.toString().equals(serverUserStats.toString()));

		testUpdateData();
	}

	@Test
	public void testInsertData() throws Throwable {
		sfdb.savepoint(INSERT_SAVEPOINT);
		assertEquals(2, sfdb.savepointCount());

		JSONArray jsonArr = new JSONArray();
		JSONObject activity1 = new JSONObject();
		activity1.put(SmartFuelActivity.TABLE.COLUMN.CORRECT_DISTANCE, "58.1");
		activity1.put(SmartFuelActivity.TABLE.COLUMN.SPEEDING_DISTANCE, "3.76");
		activity1.put(SmartFuelActivity.TABLE.COLUMN.POINTS, "55");

		JSONObject activity2 = new JSONObject();
		activity2.put(SmartFuelActivity.TABLE.COLUMN.CORRECT_DISTANCE, "31.9");
		activity2.put(SmartFuelActivity.TABLE.COLUMN.SPEEDING_DISTANCE, "2.08");
		activity2.put(SmartFuelActivity.TABLE.COLUMN.POINTS, "27");

		jsonArr.put(activity1);
		jsonArr.put(activity2);

		JSONArray activitiesBefore = sfdb.queryActivities();

		//save snapshot for later use e.g.: (in another tests)
		snapshots.put(INSERT_SAVEPOINT, activitiesBefore);

		sfdb.saveData(SmartFuelActivity.TABLE.NAME, jsonArr, SFDB.ORIGIN_LOCAL);

		JSONArray activitiesAfter = sfdb.queryActivities();

		assertFalse("Error: activities cannot match after inserting an activity",
				"".equals(activitiesAfter.toString()));

		JSONArray unsyncedActivities = sfdb.queryActivities(false);

		//takes a necessary cleanup because some values have been inserted as default by SQLite
		JSONObject tmp;
		for (int i = 0; i < unsyncedActivities.length(); i++) {
			tmp = unsyncedActivities.getJSONObject(i);
			//Tests the default values set by the database
			assertEquals("0", tmp.getString(SmartFuelActivity.TABLE.COLUMN.EXPIRED));
			assertEquals("0", tmp.getString(SmartFuelActivity.TABLE.COLUMN.SPENT));

			tmp.remove(SmartFuelActivity.TABLE.COLUMN.ID);
			tmp.remove(SmartFuelActivity.TABLE.COLUMN.CREATED_AT);
			tmp.remove(SmartFuelActivity.TABLE.COLUMN.EXPIRED);
			tmp.remove(SmartFuelActivity.TABLE.COLUMN.SPENT);
		}

		assertTrue("Error: unsynced and inserted activities do not match",
				jsonArr.toString().equals(unsyncedActivities.toString()));
	}

	@Test
	public void testRollback() {
		sfdb.rollback();

		Cursor c = sfdb.db.rawQuery("SELECT * FROM " + User.TABLE.NAME, null);

		assertFalse("Error: table " + User.TABLE.NAME + " is not empty.", c.moveToNext());

		c = sfdb.db.rawQuery("SELECT * FROM "+SmartFuelActivity.TABLE.NAME, null);

		assertFalse("Error: table " + SmartFuelActivity.TABLE.NAME + " is not empty.", c.moveToNext());

		c = sfdb.db.rawQuery("SELECT * FROM "+Event.TABLE.NAME, null);

		assertFalse("Error: table " + Event.TABLE.NAME + " is not empty.", c.moveToNext());

		c = sfdb.db.rawQuery("SELECT * FROM "+Event.CONTENT.TABLE.NAME, null);

		assertFalse("Error: table " + Event.CONTENT.TABLE.NAME + " is not empty.", c.moveToNext());

		c.close();

		prefs.edit().clear().apply();
	}

	/*
		Helper methods.
	 */

	private void testUpdateData() throws Throwable {
		JSONObject user = sfdb.queryUserData();
		user.put("name", "Andrej");
		sfdb.saveData(User.TABLE.NAME, user, SFDB.ORIGIN_LOCAL);

		JSONObject userAfterUpdate = sfdb.queryUserData(true);

		assertFalse("Error: Update has not been completed",
				user.toString().equals(userAfterUpdate.toString()));

		try {
			sfdb.sync(true);
		} catch (Exception e) {
			Assert.fail("Error: Update has not been successful");
		}
	}

	private void checkTableColumns(String table, HashSet<String> columns) {
		//database connection still OK?
		assertEquals(true, sfdb.db.isOpen());

		Cursor c = sfdb.db.rawQuery("PRAGMA table_info("+table+")", null);

		assertTrue("Error: Unable to receive " + table + " table information.", c.moveToFirst());

		int colNameIndex = c.getColumnIndex("name");
		do {
			String colName = c.getString(colNameIndex);
			columns.remove(colName);
		} while (c.moveToNext());

		c.close();

		assertTrue("Error: The table " + table + " does not contain all of the desired columns",
				columns.isEmpty());
	}

	private void testIndexExistence(String index) {
		//database connection still OK?
		assertEquals(true, sfdb.db.isOpen());

		Cursor c = sfdb.db.rawQuery("PRAGMA index_info(" + index + ")", null);

		assertTrue("Error: Index " + index + " does not exist.", c.moveToFirst());

		c.close();
	}

	private void testUserTableStructure() {
		final HashSet<String> userColumnHS = new HashSet<>();
		userColumnHS.add(User.TABLE.COLUMN.ID);
		userColumnHS.add(User.TABLE.COLUMN.NAME);
		userColumnHS.add(User.TABLE.COLUMN.SURNAME);
		userColumnHS.add(User.TABLE.COLUMN.CITY);
		userColumnHS.add(User.TABLE.COLUMN.REGION);
		userColumnHS.add(User.TABLE.COLUMN.EMAIL);
		userColumnHS.add(User.TABLE.COLUMN.CHIP_ID);
		userColumnHS.add(User.TABLE.COLUMN.CURRENT_POINTS);
		userColumnHS.add(User.TABLE.COLUMN.TOTAL_POINTS);
		userColumnHS.add(User.TABLE.COLUMN.EDITED);

		checkTableColumns(User.TABLE.NAME, userColumnHS);
	}

	private void testRoadActivityTableStructure() {
		final HashSet<String> roadActivityColumnHS = new HashSet<>();
		roadActivityColumnHS.add(SmartFuelActivity.TABLE.COLUMN.ID);
		roadActivityColumnHS.add(SmartFuelActivity.TABLE.COLUMN.POINTS);
		roadActivityColumnHS.add(SmartFuelActivity.TABLE.COLUMN.CORRECT_DISTANCE);
		roadActivityColumnHS.add(SmartFuelActivity.TABLE.COLUMN.SPEEDING_DISTANCE);
		roadActivityColumnHS.add(SmartFuelActivity.TABLE.COLUMN.CREATED_AT);
		roadActivityColumnHS.add(SmartFuelActivity.TABLE.COLUMN.EXPIRED);
		roadActivityColumnHS.add(SmartFuelActivity.TABLE.COLUMN.SPENT);
		roadActivityColumnHS.add(SmartFuelActivity.TABLE.COLUMN.SYNCHRONIZED);

		checkTableColumns(SmartFuelActivity.TABLE.NAME, roadActivityColumnHS);
	}

	private void testEventTableStructure() {
		final HashSet<String> eventColumnHS = new HashSet<>();
		eventColumnHS.add(Event.TABLE.COLUMN.ID);
		eventColumnHS.add(Event.TABLE.COLUMN.TYPE);
		eventColumnHS.add(Event.TABLE.COLUMN.CREATED_AT);
		eventColumnHS.add(Event.TABLE.COLUMN.SEEN);

		checkTableColumns(Event.TABLE.NAME, eventColumnHS);
	}

	private void testEventContentTableStructure() {
		final HashSet<String> eventContentColumnHS = new HashSet<>();
		eventContentColumnHS.add(Event.CONTENT.TABLE.COLUMN.ID);
		eventContentColumnHS.add(Event.CONTENT.TABLE.COLUMN.EVENT_ID);
		eventContentColumnHS.add(Event.CONTENT.TABLE.COLUMN.LANGUAGE);
		eventContentColumnHS.add(Event.CONTENT.TABLE.COLUMN.KEY);
		eventContentColumnHS.add(Event.CONTENT.TABLE.COLUMN.VALUE);

		checkTableColumns(Event.CONTENT.TABLE.NAME, eventContentColumnHS);
	}

	private void testStatsViewStructure() {
		final HashSet<String> statsColumnHS = new HashSet<>();
		statsColumnHS.add(Statistics.VIEW.COLUMN.POINTS);
		statsColumnHS.add(Statistics.VIEW.COLUMN.CORRECT_DISTANCE);
		statsColumnHS.add(Statistics.VIEW.COLUMN.SPEEDING_DISTANCE);
		statsColumnHS.add(Statistics.VIEW.COLUMN.TOTAL_EXPIRED);
		statsColumnHS.add(Statistics.VIEW.COLUMN.DAY);
		statsColumnHS.add(Statistics.VIEW.COLUMN.WEEK);
		statsColumnHS.add(Statistics.VIEW.COLUMN.MONTH);

		checkTableColumns(Statistics.VIEW.NAME, statsColumnHS);
	}

}
