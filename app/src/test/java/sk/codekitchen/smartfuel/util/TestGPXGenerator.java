package sk.codekitchen.smartfuel.util;

import android.content.Context;
import android.os.Build;
import android.test.AndroidTestCase;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.net.URL;

import sk.codekitchen.smartfuel.BuildConfig;

/**
 * @author Attila Večerek
 */
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricGradleTestRunner.class)
public class TestGPXGenerator extends AndroidTestCase {

	protected Context context;

	@Before
	public void setUp() {
		if(this.context == null) {
			this.context = RuntimeEnvironment.application.getApplicationContext();
		}
	}

	@Test
	public void fileObjectShouldNotBeNull() throws Exception {
		File file = Utils.getFileFromPath(this, "res/gpx_with_speed.gpx");
		assertNotNull("Error: file not loaded", file);
	}

	@Test
	public void gpxGeneratorObjectShouldBeCreated() throws Exception {
		try {
			File gpxFile = Utils.getFileFromPath(this, Utils.TEST_FILE_WITH_SPEED);
			GPXGenerator gpx = new GPXGenerator(this.context, gpxFile);
			Assert.assertEquals(gpx.getNumLocations(), gpx.getNodesNumberOf("trkpt"));

		} catch (Exception e) {
			Assert.fail("Object construction shouldn't throw an exception: " + e.getMessage());
		}
	}
}
