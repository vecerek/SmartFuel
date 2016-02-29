package sk.codekitchen.smartfuel.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;

import sk.codekitchen.smartfuel.BuildConfig;
import sk.codekitchen.smartfuel.util.GLOBALS;
import sk.codekitchen.smartfuel.util.GPXGenerator;
import sk.codekitchen.smartfuel.util.TestGPXGenerator;

/**
 * @author Attila Večerek
 */
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricGradleTestRunner.class)
public class TestRide extends AndroidTestCase {

    protected Context context;
    protected static final String TEST_FILE_WITH_SPEED = "res/gpx_with_speed.gpx";
    protected static final String TEST_FILE_WITHOUT_SPEED = "res/gpx_without_speed.gpx";

    @Before
    public void setUp() {
        if(this.context == null)
            this.context = RuntimeEnvironment.application.getApplicationContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getInt(GLOBALS.USER_ID, -1) != 1) {
            prefs.edit().putInt(GLOBALS.USER_ID, 1).commit();
        }
    }

    @Test
    public void RideObjectShouldBeCreated() throws Exception {
        GPXGenerator gpx = new GPXGenerator(this.context,
                TestGPXGenerator.getFileFromPath(this, TEST_FILE_WITHOUT_SPEED));
        try {
            Ride ride = new Ride(this.context);
            try {
                for (Location loc : gpx.getLocations()) {
                    ride.addRecord(loc);
                }
            } catch (Exception e) {
                Assert.fail("Adding records should not fail: " + e.getMessage());
            }
            System.out.println(ride.toString());
        } catch (Exception e) {
            Assert.fail("Object construction shouldn't throw an exception: " + e.getMessage());
        }
    }
}
