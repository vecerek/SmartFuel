package sk.codekitchen.smartfuel.util;

import java.io.File;
import java.net.URL;

/**
 * @author Attila Večerek
 */
public class Utils {

    protected static final String TEST_FILE_WITH_SPEED = "res/gpx_with_speed.gpx";
    protected static final String TEST_FILE_WITHOUT_SPEED = "res/gpx_without_speed.gpx";

    public static File getFileFromPath(Object obj, String fileName) {
        ClassLoader classLoader = obj.getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        return new File(resource.getPath());
    }
}
