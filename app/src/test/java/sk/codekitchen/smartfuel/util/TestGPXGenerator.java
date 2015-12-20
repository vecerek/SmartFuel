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
	protected static final String TEST_FILE_WITH_SPEED = "res/gpx_with_speed.gpx";
	protected static final String TEST_FILE_WITHOUT_SPEED = "res/gpx_without_speed.gpx";

	@Before
	public void setUp() {
		this.context = RuntimeEnvironment.application.getApplicationContext();
	}

	@Test
	public void fileObjectShouldNotBeNull() throws Exception {
		File file = getFileFromPath(this, "res/gpx_with_speed.gpx");
		assertNotNull("Error: file not loaded", file);
	}

	@Test
	public void gpxGeneratorObjectShouldBeCreated() throws Exception {
		try {
			File gpxFile = getFileFromPath(this, TEST_FILE_WITH_SPEED);
			GPXGenerator gpx = new GPXGenerator(this.context, gpxFile);
			Assert.assertEquals(gpx.getNumLocations(), gpx.getNodesNumberOf("trkpt"));

		} catch (Exception e) {
			Assert.fail("Object construction shouldn't throw an exception: " + e.getMessage());
		}
	}

	private static File getFileFromPath(Object obj, String fileName) {
		ClassLoader classLoader = obj.getClass().getClassLoader();
		URL resource = classLoader.getResource(fileName);
		return new File(resource.getPath());
	}
}
