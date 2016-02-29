package sk.codekitchen.smartfuel.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;

import junit.framework.Assert;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import sk.codekitchen.smartfuel.BuildConfig;
import sk.codekitchen.smartfuel.exception.UnknownUserException;
import sk.codekitchen.smartfuel.util.GLOBALS;
import sk.codekitchen.smartfuel.util.GPXGenerator;
import sk.codekitchen.smartfuel.util.TestGPXGenerator;

/**
 * @author Attila Večerek
 */
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricGradleTestRunner.class)
public class TestRide extends AndroidTestCase {

    protected static final String TEST_FILE_WITH_SPEED = "res/gpx_with_speed.gpx";
    protected static final String TEST_FILE_WITHOUT_SPEED = "res/gpx_without_speed.gpx";

    private Context context;
    private SFDB sfdb;

    @Before
    public void setUp() {
        if(this.context == null)
            this.context = RuntimeEnvironment.application.getApplicationContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getInt(GLOBALS.USER_ID, -1) != 1) {
            prefs.edit().putInt(GLOBALS.USER_ID, 1).commit();
        }

        try {
            if (sfdb == null) sfdb = SFDB.getInstance(context);
            sfdb.savepoint();

        } catch (UnknownUserException | ParseException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        if (sfdb != null) sfdb.rollback();
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
                try {
                    File activity = new File(ride.saveActivity());
                    Assert.assertTrue("Driving activity should have been saved.", activity.exists());
                } catch (JSONException e) {
                    Assert.fail("Parsing JSON should pass: " + e.getMessage());
                } catch (ParserConfigurationException e) {
                    Assert.fail("Invalid GPX file created: " + e.getMessage());
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                } catch (TransformerException e) {
                    Assert.fail("Content could not be written into file: " + e.getMessage());
                }

            } catch (Exception e) {
                System.out.println("Exception class: " + e.getClass());
                e.printStackTrace();
                Assert.fail("Adding records should not fail: " + e.getMessage());
            }
            System.out.println(ride.toString());
        } catch (Exception e) {
            Assert.fail("Object construction shouldn't throw an exception: " + e.getMessage());
        }
    }
}
