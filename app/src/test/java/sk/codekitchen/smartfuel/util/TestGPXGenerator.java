package sk.codekitchen.smartfuel.util;

import android.os.Build;
import android.test.AndroidTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
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

	@Test
	public void fileObjectShouldNotBeNull() throws Exception {
		File file = getFileFromPath(this, "res/gpx_with_speed.gpx");
		assertNotNull("Error: file not loaded", file);
	}

	private static File getFileFromPath(Object obj, String fileName) {
		ClassLoader classLoader = obj.getClass().getClassLoader();
		URL resource = classLoader.getResource(fileName);
		return new File(resource.getPath());
	}
}
