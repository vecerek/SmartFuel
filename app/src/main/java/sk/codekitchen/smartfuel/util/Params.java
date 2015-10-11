package sk.codekitchen.smartfuel.util;

/**
 * Params class serves as a container forSharedPreferences keys.
 *
 * @author Attila Večerek
 * @since 1.0
 */
public final class Params {

	public static final String USER_ID = "user_id";
	public static final String LAST_UPDATE = "last_update";
	public static final String BAD_EMAIL = "Account does not exist.";
	public static final String BAD_PASS = "Incorrect password.";
	public static final String ERROR_MSG_KEY = "error_msg";

	public static final class PARAM_KEY {
		public static final String TOTAL_SUCCESS_RATE = "total_success_rate";
		public static final String TOTAL_DISTANCE = "total_distance";
		public static final String TOTAL_EXPIRED_POINTS = "total_expired_points";
	}

	public static final class HTTP_RESPONSE {
		public static final int OK = 200;
		public static final int FORBIDDEN = 403;
	}
}
