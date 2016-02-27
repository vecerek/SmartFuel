package sk.codekitchen.smartfuel.util;

/**
 * @author Attila Večerek
 */
public final class Units {
    public static final class Speed {
        public static final class Mps {
            public static float toKph(float val) { return val * GLOBALS.CONST.MPS2KPH; }
            public static float toMph(float val) { return Kph.toMph(toKph(val)); }
        }

        public static class Kph {
            public static float toMph(float val) { return val * GLOBALS.CONST.KM2MI; }
        }
    }

    public static final class Distance {
        public static final class Meter {
            public static float toKm(float val) { return val/1000; }
            public static float toMi(float val) { return Km.toMi(toKm(val)); }
        }

        public static final class Km {
            public static float toMi(float val) { return val * GLOBALS.CONST.KM2MI; }
            public static float toMe(float val) { return val * GLOBALS.CONST.M2KM; }
        }

        public static final class Mi {
            public static float toFt(float val) { return val * GLOBALS.CONST.MI2FT; }
        }
    }

    /**
     * Returns the speed in the preferred unit.
     * @param speedInMps
     * @return
     */
    public static float getPreferredSpeed(float speedInMps, boolean isMph) {
        return isMph
                ? Units.Speed.Mps.toMph(speedInMps)
                : Units.Speed.Mps.toKph(speedInMps);
    }

    public static int getPreferredSpeedLimit(int speedLimit, boolean isMph) {
        return isMph
                ? 5 * Math.round(Units.Speed.Kph.toMph(speedLimit) / 5f)
                : speedLimit;
    }

    public static float getPreferredDistance(float dist, boolean isMph) {
        return isMph
                ? Units.Distance.Meter.toMi(dist)
                : Units.Distance.Meter.toKm(dist);
    }
}
