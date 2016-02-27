package sk.codekitchen.smartfuel.util;

/**
 * @author Attila Večerek
 */
public final class Units {
    public static final class Speed {
        public static int toImperial(int val) {
            return Math.round(val * GLOBALS.CONST.KM2MI);
        }

        // TODO: Check out, what numbers we get in statistics, if each ends with .0, we should round it to a 1 decimal precision
        public static float toImperial(float val) {
            return Math.round(val * GLOBALS.CONST.KM2MI);
        }
    }
}
