package sk.codekitchen.smartfuel.model;
import sk.codekitchen.smartfuel.exception.DuplicateSavepointException;
import sk.codekitchen.smartfuel.exception.TableNotFoundException;
import sk.codekitchen.smartfuel.exception.UnknownDataOriginException;
import sk.codekitchen.smartfuel.exception.UnknownUserException;
import sk.codekitchen.smartfuel.util.GLOBALS;
import sk.codekitchen.smartfuel.util.ServerAPI;
import sk.codekitchen.smartfuel.util.cJSONArray;
import sk.codekitchen.smartfuel.util.cJSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Keeps the local database consistent with the one on the server.
 * Also works as an implemented interface for data querying, with
 * prepared queries like {@link #queryUserData()}, {@link #queryActivities()},
 * {@link #queryStats()}, or {@link #queryEvents()}.
 *
 * SFDB stands for SmartFuel DataBase.
 *
 * @author Attila Večerek
 * @since 1.0
 */
public class SFDB extends SQLiteOpenHelper {

	protected SQLiteDatabase db;
	protected Long lastInsertedId = null;

	public static final String DATABASE_NAME = "smartfuel";
	public static final int DATABASE_VERSION = 1;
	public static final String SAVEPOINT = "savepoint_";
	public static final String ORIGIN_LOCAL = "local";
	public static final String ORIGIN_SERVER = "server";
	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	protected int userID;
	protected Context ctx;

	private Boolean transactionExists = false;
	private Date lastUpdate;
	private SharedPreferences preferences;
	private Vector<String> savepoints;

	/**
	 * Constructs a new SFDB instance with the given context.
	 *
	 * @param context makes able to get the user ID
	 * @throws SQLiteException if there is a lack of space on the disk
	 * @throws ParseException if the date can't be formatted as desired
	 * @throws UnknownUserException if the sharedPreferences does not contain the user's ID
	 * @since 1.0
	 */
	public SFDB(Context context)
			throws SQLiteException, ParseException, UnknownUserException {

		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		ctx = context;
		savepoints = new Vector<>(5, 10);
		//Set date format to UTC
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));

		preferences = PreferenceManager.getDefaultSharedPreferences(ctx);

		userID = preferences.getInt(GLOBALS.USER_ID, -1);
		if(userID == -1)
			throw new UnknownUserException("Unknown user");

		String tmpLastUpdate = preferences.getString(GLOBALS.LAST_UPDATE, null);
		lastUpdate = tmpLastUpdate == null ? null : DATE_FORMAT.parse(tmpLastUpdate);

		db = this.getWritableDatabase();
	}

	/** {@inheritDoc}
	 * Creates tables, views and fills them up with data.
	 * @param db
	 * @since 1.0
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(User.TABLE.CREATE);
		db.execSQL(SmartFuelActivity.TABLE.CREATE);
		db.execSQL(Event.TABLE.CREATE);
		db.execSQL(Event.CONTENT.TABLE.CREATE);
		db.execSQL(Event.CONTENT.TABLE.CREATE_INDEX);
		db.execSQL(Statistics.VIEW.CREATE);
	}

	/** {@inheritDoc}
	 * Drops current DB structure and recreates it based on the new version.
	 * @param db
	 * @param oldVersion
	 * @param newVersion
	 * @since 1.0
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(SFDB.class.getName(), "Upgrading database from version " + oldVersion + "to version " + newVersion);
		db.execSQL(User.TABLE.DROP);
		db.execSQL(Statistics.VIEW.DROP);
		db.execSQL(SmartFuelActivity.TABLE.DROP);
		db.execSQL(Event.TABLE.DROP);
		db.execSQL(Event.CONTENT.TABLE.DROP);
		onCreate(db);
	}

	/**
	 * @return user's ID
	 */
	public int getUserID() { return this.userID; }

	/**
	 * @return the last inserted id into the database
	 */
	public Long lastInsertedId() { return this.lastInsertedId; }

	/**
	 * Begins a database transaction.
	 * @since 1.0
	 */
	public void beginTransaction() {
		if (!transactionExists) {
			db.execSQL("BEGIN TRANSACTION;");
			transactionExists = true;
		}
	}

	/**
	 * Commits changes made upon the database.
	 * @since 1.0
	 */
	public void commit() {
		if (transactionExists) {
			db.execSQL("COMMIT;");
			transactionExists = false;
		}
	}

	/**
	 * {@code name} defaults to 'savepoint_x', where x is its order starting at 0
	 * @see #savepoint(String)
	 * @since 1.0
	 */
	public void savepoint()
			throws DuplicateSavepointException {
		String name = SAVEPOINT;
		int order = savepoints.capacity();
		savepoint(name + String.valueOf(order));
	}

	/**
	 * Creates a named SQLite savepoint.
	 *
	 * @param name savepoint's name
	 * @throws DuplicateSavepointException, if savepoint already exists
	 * @since 1.0
	 */
	public void savepoint(String name)
			throws DuplicateSavepointException {
		if (savepoints.contains(name)) {
			throw new DuplicateSavepointException(name);
		} else {
			savepoints.add(name);
		}
	}

	/**
	 * Rolls back the changes made upon the database.
	 * @since 1.0
	 */
	public void rollback() {
		if (transactionExists) {
			db.execSQL("ROLLBACK;");
			transactionExists = false;
		}
	}

	/**
	 * Rolls back to the last known savepoint.
	 *
	 * @see #rollbackTo(String)
	 * @since 1.0
	 */
	public void rollbackTo() {
		if(!savepoints.isEmpty()) {
			db.execSQL("ROLLBACK TO " + savepoints.lastElement());
		}
	}

	/**
	 * Rolls back to the specified savepoint.
	 *
	 * @param savepoint savepoint's name to rollback to
	 * @since 1.0
	 */
	public void rollbackTo(String savepoint) {
		int index;
		if(!savepoints.isEmpty()) {
			if ((index = savepoints.indexOf(savepoint)) != -1) {
				int last = savepoints.indexOf(savepoints.lastElement());
				for (int i = index; i < last; i++) {
					savepoints.remove(i+1);
				}
				rollbackTo();
			}
		}
	}

	/**
	 * Returns the number of savepoints created.
	 * Used mainly for testing purposes.
	 *
	 * @return number of savepoints acknowledged
	 * @since 1.0
	 */
	public int savepointCount() {
		return savepoints.size();
	}

	/**
	 * Returns requested database data from the server.
	 * Initial data download.
	 *
	 * @return the server's response data
	 * @throws IOException if database connection could not be established
	 *         or the server's response could not be parsed.
	 * @since 1.0
	 */
	protected JSONObject downloadDatabase() throws IOException { return downloadDatabase(null); }

	/**
	 * Returns requested database data from the server based on the last update time.
	 *
	 * @param lastUpdate last database update time
	 * @return the server's response data
	 * @throws IOException if database connection could not be established
	 *         or the server's response could not be parsed.
	 * @since 1.0
	 */
	protected JSONObject downloadDatabase(String lastUpdate) throws IOException {
		try {
			Map<String, String> params = new HashMap<>();
			params.put(GLOBALS.USER_ID, Integer.toString(userID));
			if(lastUpdate != null) {
				params.put(GLOBALS.LAST_UPDATE, lastUpdate);
			}

			return (new ServerAPI("download_db").sendRequest(params));

		} catch (JSONException e) {
			throw new IOException("Server response could not be parsed", e);
		} catch (IOException e) {
			throw new IOException("Database connection error.", e);
		}
	}

	/**
	 * Saves an array of JSON data into a table based on the origin of the update.
	 * Update can be either {@value #ORIGIN_LOCAL} or {@value #ORIGIN_SERVER}
	 *
	 * @see #saveData(String, JSONObject, String)
	 * @since 1.0
	 */
	public void saveData(String table, JSONArray dataArray, String origin)
			throws JSONException, TableNotFoundException, UnknownDataOriginException,
			SQLiteCantOpenDatabaseException {

		for (int i = 0; i < dataArray.length(); i++) {
			saveData(table, dataArray.getJSONObject(i), origin);
		}

	}

	/**
	 * Saves JSON data into a table based on the origin os the update.
	 *
	 * @param table table's name
	 * @param data array of JSON data
	 * @param origin updates's origin
	 * @throws JSONException if the data can't be read properly
	 * @throws TableNotFoundException if the table does not exist
	 * @throws UnknownDataOriginException if the data's origin is unknown
	 * @throws SQLiteCantOpenDatabaseException if the database could not be opened
	 * @since 1.0
	 */
	public void saveData(String table, JSONObject data, String origin)
			throws JSONException, TableNotFoundException, UnknownDataOriginException,
			SQLiteCantOpenDatabaseException {

		checkOrigin(origin);
		ContentValues cv;
		int id = -1;
		String idCol;
		switch (table) {
			case User.TABLE.NAME:
				//local changes edit the data, server changes update them
				Boolean edited = origin.equals(ORIGIN_LOCAL);
				cv = User.TABLE.getContentValues(data, edited);
				idCol = User.TABLE.COLUMN.ID;
				if (data.has(idCol)) id = data.getInt(idCol);
				break;
			case SmartFuelActivity.TABLE.NAME:
				//server updates synchronize the data, local changes desynchronize them
				Boolean _synchronized = origin.equals(ORIGIN_SERVER);
				cv = SmartFuelActivity.TABLE.getContentValues(data, _synchronized);
				idCol = SmartFuelActivity.TABLE.COLUMN.ID;
				if (data.has(idCol)) id = data.getInt(idCol);
				break;
			case Event.TABLE.NAME:
				cv = Event.TABLE.getContentValues(data);
				idCol = Event.TABLE.COLUMN.ID;
				if (data.has(idCol)) id = data.getInt(idCol);
				break;
			case Event.CONTENT.TABLE.NAME:
				cv = Event.CONTENT.TABLE.getContentValues(data);
				idCol = Event.CONTENT.TABLE.COLUMN.ID;
				if (data.has(idCol)) id = data.getInt(idCol);
				break;
			default:
				throw new TableNotFoundException(table);
		}

		if (id != -1) {
			if (idExists(table, idCol, id)) {
				//UPDATE DATA
				String where = idCol + "=?";
				String params[] = {String.valueOf(id)};
				db.update(table, cv, where, params);
			} else {
				//INSERT DATA
				lastInsertedId = db.insert(table, null, cv);
			}
		} else {
			//INSERT DATA
			lastInsertedId = db.insert(table, null, cv);
		}
	}

	/**
	 * Checks the origin of the update.
	 *
	 * @param origin update's origin
	 * @throws UnknownDataOriginException if the origin is not an acceptable one
	 * @since 1.0
	 */
	protected void checkOrigin(String origin) throws UnknownDataOriginException {
		if (!(origin.equals(ORIGIN_LOCAL) || origin.equals(ORIGIN_SERVER)))
			throw new UnknownDataOriginException(origin);
	}

	/**
	 * A synonym function call for {@link #valueExists(String, String, Object)}
	 *
	 * @since 1.0
	 */
	protected boolean idExists(String table, String col, int value)
			throws SQLiteCantOpenDatabaseException {

		return valueExists(table, col, value);
	}

	/**
	 * Checks whether a column value exists in the passed table.
	 *
	 * @param table table's name
	 * @param col column's name
	 * @param value value to be checked
	 * @return true if the value exists
	 *         false if not
	 * @throws SQLiteCantOpenDatabaseException if the database could not be opened
	 * @since 1.0
	 */
	protected boolean valueExists(String table, String col, Object value)
			throws SQLiteCantOpenDatabaseException {
		String where = col + "=?";
		String[] params = {String.valueOf(value)};
		Cursor c = db.query(table, null, where, params, null, null, null, null);
		if (c != null) {
			boolean exists = c.getCount() > 0;
			c.close();
			return exists;
		} else {
			throw new SQLiteCantOpenDatabaseException();
		}
	}

	/**
	 * Sets the last update time of the local database.
	 * It's a matter of data consistency protection method.
	 *
	 * @since 1.0
	 */
	protected void setLastUpdateTime() {
		lastUpdate = new Date(); //milliseconds since Unix epoch, UTC
		preferences.edit().putString(GLOBALS.LAST_UPDATE, DATE_FORMAT.format(lastUpdate)).apply();
	}

	public void sync()
			throws IOException, UnknownUserException,
			ParserConfigurationException, SAXException, ParseException { sync(false); }

	/**
	 * Synchronizes the application's local database with the server's one and vice versa.
	 * Implements the core of the data consistency protection.
	 *
	 * @param test indicates if the sync is for testing purposes only
	 * @throws IOException if the server database could not be updated
	 *                     if the local database could not be updated
	 *                     if the data returned by the server were corrupted
	 * @since 1.0
	 */
	public void sync(boolean test)
			throws IOException, UnknownUserException, ParserConfigurationException,
			SAXException, ParseException {

		try {
			//check, if there are any pending activities, that need to be evaluated
			SmartFuelActivity.evaluatePendingActivities(ctx);

			JSONObject editedData = queryEditedData();
			JSONObject result;

			if (editedData != null) {
				Map<String, String> params = new HashMap<>();
				params.put(GLOBALS.USER_ID, Integer.toString(userID));
				params.put("data", editedData.toString());
				if (test) params.put("test", String.valueOf(true));

				//TODO: upload the unsynchronized activities' GPX files to the server, if they exist
				result = (new ServerAPI("update_db")).sendRequest(params);

				if (result.has("success"))
					if (!result.getBoolean("success"))
						throw new IOException("Server database update failed");
			}

			if (lastUpdate == null) {
				result = downloadDatabase();
			} else {
				result = downloadDatabase(DATE_FORMAT.format(lastUpdate));
			}

			if (result.has("success")) {
				if (result.getBoolean("success") && result.has("data")) {
					JSONObject data = result.getJSONObject("data");
					String key;

					beginTransaction();
					try {
						for (Iterator<String> keys = data.keys(); keys.hasNext(); ) {
							key = keys.next();
							saveData(key, data.getJSONArray(key), ORIGIN_SERVER);
						}
					} catch (TableNotFoundException | JSONException e) {
						rollback();
						throw e;
					}
					commit();

					if (result.has(GLOBALS.PARAM_KEY.PROFILE_PIC_URL)) {
						String url = result.getString(GLOBALS.PARAM_KEY.PROFILE_PIC_URL);
						Bitmap profilePicture = BitmapFactory.decodeStream(
								(InputStream) new URL(url).getContent());
						User.saveProfilePicture(ctx, profilePicture);
					}
				} else {
					throw new IOException("Local database update failed");
				}

				setLastUpdateTime();
			} else {
				throw new IOException("Data returned by the server are corrupted, repeat the action");
			}

		} catch (TableNotFoundException | JSONException e) {
			e.printStackTrace();
			throw new IOException("Updating data has suddenly failed due to internal error, bad programming", e);
		}
	}

	/*
	 *
	 * The section beyond this comment contains private and public database queries.
	 *             Insert your queries only beyond this comment.
	 *
	 */

	/**
	 * Returns the edited and not yet synchronized data from the local database.
	 *
	 * @return the edited and not yet synchronized local data
	 *         null, if there are no data to be returned
	 * @since 1.0
	 */
	protected JSONObject queryEditedData()
			throws JSONException {

		JSONObject user = queryUserData(true);
		JSONArray activities = queryActivities(false);

		if (user != null || activities != null) {
			JSONObject result = new JSONObject();
			if (user != null) {
				result.put("user", user);
			}
			if (activities != null) {
				result.put("activities", activities);
			}

			return result;

		} else {
			return null;
		}
	}

	/**
	 * {@code edited} defaults to false
	 *
	 * @see #queryUserData(Boolean)
	 */
	public JSONObject queryUserData() { return queryUserData(null); }

	/**
	 * Returns and retrieves user's data based on the edition preference.
	 *
	 * @param edited edition preference
	 * @return all user data, if {@code edited} is null
	 *         only the edited user data, if {@code edited} is true
	 *         only the non-edited user data, if {@code edited} is false
	 *         null, if not data are to be returned
	 * @since 1.0
	 */
	public JSONObject queryUserData(Boolean edited) {
		JSONObject data = null;
		String where = User.TABLE.COLUMN.ID + "=?";
		String[] params = {String.valueOf(userID)};

		if (edited != null) {
			where += " AND " + User.TABLE.COLUMN.EDITED + "=?";
			params = new String[] {String.valueOf(userID), String.valueOf(edited ? 1 : 0)};
		}

		Cursor cursor = db.query(User.TABLE.NAME, null, where, params, null, null, null, null);

		if(cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				data = new cJSONObject(cursor);
				cursor.close();
			}
		}

		return data;
	}

	/**
	 * Returns a JSONObject containing the user profile data
	 *
	 * @return success_rate, total_km, total_expired
	 * @since 1.0
	 */
	public JSONObject queryProfileData() {
		final String succRate = GLOBALS.PARAM_KEY.TOTAL_SUCCESS_RATE;
		final String totKm = GLOBALS.PARAM_KEY.TOTAL_DISTANCE;
		final String totEx = GLOBALS.PARAM_KEY.TOTAL_EXPIRED_POINTS;

		Cursor cursor = db.rawQuery(
		"SELECT ROUND(100 * (d.corr_dist / (d.corr_dist+d.speed_dist))) AS "+succRate+", " +
				"ROUND(d.corr_dist+d.speed_dist) AS "+totKm+", " + totEx + " " +
				"FROM (" +
					"SELECT SUM(" + SmartFuelActivity.TABLE.COLUMN.CORRECT_DISTANCE + ") AS corr_dist, " +
					"SUM(" + SmartFuelActivity.TABLE.COLUMN.SPEEDING_DISTANCE + ") AS speed_dist, " +
					"SUM("+SmartFuelActivity.TABLE.COLUMN.POINTS+"*"+SmartFuelActivity.TABLE.COLUMN.EXPIRED+") as " + totEx + " " +
					"FROM " + SmartFuelActivity.TABLE.NAME + ") AS d", null
		);

		JSONObject data = null;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				data = new cJSONObject(cursor);
			}
			cursor.close();
		}

		return data;
	}

	/**
	 * {@code _synchronized} defaults to null
	 *
	 * @see #queryActivities(Boolean)
	 */
	public JSONArray queryActivities() { return queryActivities(null); }

	/**
	 * Returns user's activities based on the synchronization preference.
	 * @param _synchronized synchronization preference
	 * @return all user's activities if {@code _synchronized} is null
	 *         only the synchronized user activities if {@code _synchronized} is true
	 *         only the unsynchronized user activities if {@code _synchronized} is false
	 *         null, if no data are to be returned
	 * @since 1.0
	 */
	public JSONArray queryActivities(Boolean _synchronized) {
		Cursor cursor;
		String[] projection = new String[] {
				SmartFuelActivity.TABLE.COLUMN.ID,
				SmartFuelActivity.TABLE.COLUMN.CORRECT_DISTANCE,
				SmartFuelActivity.TABLE.COLUMN.SPEEDING_DISTANCE,
				SmartFuelActivity.TABLE.COLUMN.CREATED_AT,
				SmartFuelActivity.TABLE.COLUMN.POINTS,
				SmartFuelActivity.TABLE.COLUMN.EXPIRED,
				SmartFuelActivity.TABLE.COLUMN.SPENT
		};

		if (_synchronized != null) {
			String where = SmartFuelActivity.TABLE.COLUMN.SYNCHRONIZED + "=?";
			String[] params = {String.valueOf(_synchronized ? 1 : 0)};
			cursor = db.query(SmartFuelActivity.TABLE.NAME, projection, where, params, null, null, null, null);
		} else {
			cursor = db.query(SmartFuelActivity.TABLE.NAME, projection, null, null, null, null, null, null);
		}

		cJSONArray result = new cJSONArray(cursor);
		if (!cursor.isClosed()) cursor.close();
		if (result.length() == 0) result = null;

		return result;
	}

	/**
	 * Returns user statistics.
	 *
	 * @return the user's statistics
	 * @see #parseStats(JSONArray) to get acquainted with the format
	 * @since 1.0
	 */
	public JSONObject queryStats() throws JSONException {
		Cursor cursor = db.query(Statistics.VIEW.NAME, null, null, null, null, null, null, null);
		JSONObject stats = parseStats(new cJSONArray(cursor));
		cursor.close();

		return stats;
	}

	/**
	 * Will return the company events' data.
	 * @return the events' data
	 * @since 1.0
	 */
	public JSONObject queryEvents() {
		//TODO: write a query for getting the events' data/content
		return null;
	}

	/**
	 * Returns parsed user statistics data.
	 *
	 * <p>
	 * The format of the returned data is following:
	 *  <pre>
	 *  {@code
	 *      &#123;
	 *          "week": &#123;    -- the keys represent the day of week(1-7), where 1 is Monday, 7 is Sunday
	 *              "1": &#123;
	 *                  "points": String,        -- represents integer
	 *                  "correct_dist": String,  -- represents float
	 *                  "speeding_dist": String, -- represents float
	 *                  "total_expired": String  -- represents integer
	 *              &#125;,
	 *              ...
	 *          &#125;,
	 *          "month": &#123;   -- the keys represent the current month's weeks of the year(1-53) according to the ISO definition
	 *              "xy": &#123;
	 *                  "points": String,
	 *                  "correct_dist": String,
	 *                  "speeding_dist": String,
	 *                  "total_expired": String
	 *              &#125;,
	 *              ...
	 *          &#125;,
	 *          "year": &#123;    -- the keys represent the months of year(1-12)
	 *              "1": &#123;
	 *                  "points": String,
	 *                  "correct_dist": String,
	 *                  "speeding_dist": String,
	 *                  "total_expired": String
	 *              &#125;,
	 *              ...
	 *          &#125;
	 *      &#125;
	 *  }
	 *  </pre>
	 * </p>
	 *
	 * @param stats user statistics as raw json data from the local database
	 * @return the parsed user statistics data
	 * @since 1.0
	 */
	protected JSONObject parseStats(JSONArray stats) throws JSONException {
		JSONObject parsed = new JSONObject();
		JSONObject week = new JSONObject();
		JSONObject month = new JSONObject();
		JSONObject year = new JSONObject();

		Integer[] weeks = getISOWeeksOfMonth();
		//fills up the week with blank days
		for (int i = 1; i <= 7; i++) week.put(String.valueOf(i), JSONObject.NULL);
		//fills up the month with blank ISOWeeks
		for (Integer weekNum : weeks) month.put(String.valueOf(weekNum), JSONObject.NULL);
		//fills up the year with blank months
		for (int i = 1; i <= 12; i++) year.put(String.valueOf(i), JSONObject.NULL);

		JSONObject tmp;
		for (int i = 0; i < stats.length(); i++) {
			tmp = stats.getJSONObject(i);
			if (tmp.has(Statistics.VIEW.COLUMN.DAY)) {
				String d = tmp.getString(Statistics.VIEW.COLUMN.DAY);
				d = removeStartingZero(d);
				tmp.remove(Statistics.VIEW.COLUMN.DAY);
				week.put(d, tmp);
			} else if (tmp.has(Statistics.VIEW.COLUMN.WEEK)) {
				String w = tmp.getString(Statistics.VIEW.COLUMN.WEEK);
				w = removeStartingZero(w);
				tmp.remove(Statistics.VIEW.COLUMN.WEEK);
				month.put(w, tmp);
			} else if (tmp.has(Statistics.VIEW.COLUMN.MONTH)) {
				String m = tmp.getString(Statistics.VIEW.COLUMN.MONTH);
				m = removeStartingZero(m);
				tmp.remove(Statistics.VIEW.COLUMN.MONTH);
				year.put(m, tmp);
			}
		}

		parsed.put("week", week);
		parsed.put("month", month);
		parsed.put("year", year);

		return parsed;
	}

	/**
	 * Removes the starting zero of a specified string, if it exists.
	 *
	 * @param s the string
	 * @return the string with removed starting 0
	 */
	protected String removeStartingZero(String s) {
		return s.substring(0, 1).equals("0") ? s.substring(1) : s;
	}

	/**
	 * {@code month} defaults to current month
	 *
	 * @see #getISOWeeksOfMonth(int)
	 */
	protected Integer[] getISOWeeksOfMonth() {
		return getISOWeeksOfMonth(Calendar.getInstance().get(Calendar.MONTH));
	}

	/**
	 * Returns the ISO weeks of the specified month of the current year.
	 *
	 * @param month number of month of the current year (0-11)
	 * @return array of ISO week numbers
	 */
	protected Integer[] getISOWeeksOfMonth(int month) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, month);

		int lastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DAY_OF_MONTH, 1);

		int firstWeekOfMonth = cal.get(Calendar.WEEK_OF_YEAR);
		cal.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
		int lastWeekOfMonth = cal.get(Calendar.WEEK_OF_YEAR);

		Set<Integer> ISOWeekNumbers = new HashSet<>();
		for (int i = firstWeekOfMonth; i <= lastWeekOfMonth; i++) {
			ISOWeekNumbers.add(i);
		}

		return ISOWeekNumbers.toArray(new Integer[ISOWeekNumbers.size()]);
	}

}
