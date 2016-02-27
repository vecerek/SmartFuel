package sk.codekitchen.smartfuel.util;

/**
 * GLOBALS class serves as a container forSharedPreferences keys.
 *
 * @author Attila Večerek
 * @since 1.0
 */
public final class GLOBALS {

	public static final String USER_ID = "user_id";
	public static final String LAST_UPDATE = "last_update";
	public static final String BAD_EMAIL = "Account does not exist.";
	public static final String BAD_PASS = "Incorrect password.";
	public static final String ERROR_MSG_KEY = "error_msg";
	public static final String SETTINGS_IS_MPH = "is_mph";
	public static final String SETTINGS_IS_AUDIO = "is_audio";

	public static final class CONST {
		public static final float KM2MI = 0.621371192f;
        public static final float MI2KM = 1.609344f;
        public static final float MPS2KPH = 3.6f;
	}

	public static final class PARAM_KEY {
		public static final String TOTAL_SUCCESS_RATE = "total_success_rate";
		public static final String TOTAL_DISTANCE = "total_distance";
		public static final String TOTAL_EXPIRED_POINTS = "total_expired_points";

		public static final String PROFILE_PIC_URL = "profile_pic_url";
	}

	public static final class DIR {
		public static final String PROFILE_PIC = "profile_pic";
	}

	public static final class FILE {
		public static final String PROFILE_PIC = "profile.jpg";
	}

	public static final class HTTP_RESPONSE {
		public static final int OK = 200;
		public static final int FORBIDDEN = 403;
	}

	public static final class IPC_MESSAGE_KEY {
		public static final String SPEED = "speed";
		public static final String PROGRESS = "progress";
		public static final String LIMIT = "speedlimit";
	}
}
