package sk.codekitchen.smartfuel.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.text.ParseException;

import sk.codekitchen.smartfuel.BuildConfig;
import sk.codekitchen.smartfuel.exception.IncorrectPasswordException;
import sk.codekitchen.smartfuel.exception.UnknownUserException;
import sk.codekitchen.smartfuel.util.GLOBALS;
import sk.codekitchen.smartfuel.util.ServerAPI;

/**
 * @author Attila Večerek
 */
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricGradleTestRunner.class)
public class TestUser extends AndroidTestCase {

	private Context context;

	@Before
	public void setUp() {
		context = RuntimeEnvironment.application.getApplicationContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (prefs.getInt(GLOBALS.USER_ID, -1) != 1) {
			prefs.edit().putInt(GLOBALS.USER_ID, 1).commit();
		}
	}

	@Test
	public void testUserInstantiation()
			throws Throwable {

		try {
			(new SFDB(context)).sync();

			User user = new User(context);

			JSONObject server_json = new ServerAPI("test/user_profile_data/" + user.id).sendRequest(null);

			assertEquals(server_json.getString(User.TABLE.COLUMN.NAME), user.name);
			assertEquals(server_json.getString(User.TABLE.COLUMN.SURNAME), user.surname);
			assertEquals(server_json.getString(User.TABLE.COLUMN.CITY), user.city);
			assertEquals(server_json.getString(User.TABLE.COLUMN.REGION), user.region);
			assertEquals(server_json.getString(User.TABLE.COLUMN.EMAIL), user.email);
			assertEquals(server_json.getString(User.TABLE.COLUMN.CHIP_ID), user.chipId);
			assertEquals(server_json.getInt(User.TABLE.COLUMN.TOTAL_POINTS), user.totalPoints);
			assertEquals(server_json.getInt(User.TABLE.COLUMN.CURRENT_POINTS), user.currentPoints);
			assertEquals(server_json.getInt(GLOBALS.PARAM_KEY.TOTAL_DISTANCE), user.totalDistance);
			assertEquals(server_json.getInt(GLOBALS.PARAM_KEY.TOTAL_EXPIRED_POINTS), user.totalExpiredPoints);
			assertEquals(server_json.getInt(GLOBALS.PARAM_KEY.TOTAL_SUCCESS_RATE), user.totalSuccessRate);
			assertNotNull("Error: last sync time is null", user.lastSync);
			assertEquals(0, user.refuelCount);

		} catch (UnknownUserException e) {
			Assert.fail("Error: user is unknown.");
			e.printStackTrace();
		} catch (JSONException e) {
			Assert.fail("Error: json parsing problem.");
			e.printStackTrace();
		} catch (ParseException e) {
			Assert.fail("Error: date parsing problem.");
			e.printStackTrace();
		}
	}

	@Test
	public void testAuthenticationShouldSucceed()
			throws Throwable {

		String email = "attila.vecerek@smartfuel.com";
		String password = "test_password";
		int expectedUserID = 1;

		int userID = User.authenticate(email, password);
		assertEquals(expectedUserID, userID);
	}

	@Test
	public void testAuthenticationShouldFailOnEmail()
			throws Throwable {

		String email = "attilavecerek@smartfuel.com";
		String password = "test_password";

		try {
			int userID = User.authenticate(email, password);
			Assert.fail("Should have thrown UnknownUserException");
		} catch (UnknownUserException e) {
			assertEquals("Error: Exception message is not the expected one",
					GLOBALS.BAD_EMAIL, e.getMessage());
		}
	}

	@Test
	public void testAuthenticateShouldFailOnPassword()
			throws Throwable {

		String email = "attila.vecerek@smartfuel.com";
		String password = "bad_test_password";

		try {
			int userID = User.authenticate(email, password);
			Assert.fail("Should have thrown IncorrectPasswordException");
		} catch (IncorrectPasswordException e) {
			assertEquals("Error: Exception message is not the expected one",
					GLOBALS.BAD_PASS, e.getMessage());
		}
	}
}
