package sk.codekitchen.smartfuel.util;

import java.io.File;
import java.net.URL;

/**
 * @author Attila Večerek
 */
public class Utils {

    public static File getFileFromPath(Object obj, String fileName) {
        ClassLoader classLoader = obj.getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        return new File(resource.getPath());
    }
}
