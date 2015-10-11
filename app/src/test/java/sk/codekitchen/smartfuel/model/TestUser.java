package sk.codekitchen.smartfuel.model;

import android.os.Build;
import android.test.AndroidTestCase;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import sk.codekitchen.smartfuel.BuildConfig;
import sk.codekitchen.smartfuel.exception.IncorrectPasswordException;
import sk.codekitchen.smartfuel.exception.UnknownUserException;
import sk.codekitchen.smartfuel.util.GLOBALS;

/**
 * @author Attila Večerek
 */
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricGradleTestRunner.class)
public class TestUser extends AndroidTestCase {

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
