package sk.codekitchen.smartfuel.util;

import android.location.Location;
import android.os.Build;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import sk.codekitchen.smartfuel.BuildConfig;

/**
 * @author Attila Večerek
 */
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricGradleTestRunner.class)
public class TestReverseGeocode {

    @Test
    public void testAPICall() throws Throwable {
        try {
            Location loc = new Location("GPS_PROVIDER");
            loc.setLatitude(41.321767);
            loc.setLongitude(-72.317576);

            ReverseGeocoder rgc = new ReverseGeocoder().setPosition(loc);
            int speedLimit = rgc.getSpeedLimit(true);
            String roadUse = rgc.getRoadUse();

            Assert.assertEquals(speedLimit, 65);
            Assert.assertEquals(roadUse, "LimitedAccess");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("API call should not fail.");
        }
    }
}
