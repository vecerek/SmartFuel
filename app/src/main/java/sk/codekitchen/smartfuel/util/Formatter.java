package sk.codekitchen.smartfuel.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * @author Attila Večerek
 */
public final class Formatter {

    private static DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();

    public static String format(Number val, char grouping, RoundingMode rm) {
        return format(val, "#,###.#", grouping, rm);
    }

    public static String format(Number val, String format, char grouping, RoundingMode rm) {
        symbols.setGroupingSeparator(grouping);
        DecimalFormat formatter = new DecimalFormat(format, symbols);
        formatter.setRoundingMode(rm);
        return formatter.format(val);
    }

    public static final class Distance {
        public static String format(Number val, String format) {
            return Formatter.format(val, format, ' ', RoundingMode.FLOOR);
        }

        public static String format(Number val) {
            return format(val, "#,###");
        }
    }

    public static final class Speed {
        public static String format(Number val) {
            return Formatter.format(val, "#,###", ' ', RoundingMode.HALF_UP);
        }
    }
}
