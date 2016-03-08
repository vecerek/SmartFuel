package sk.codekitchen.smartfuel.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import sk.codekitchen.smartfuel.exception.IncorrectPasswordException;
import sk.codekitchen.smartfuel.exception.UnknownUserException;
import sk.codekitchen.smartfuel.util.GLOBALS;
import sk.codekitchen.smartfuel.util.ServerAPI;

/**
 * Retrieves and handles the user related data.
 * Both profile and statistics data.
 *
 * @author Attila Večerek
 */
public class User {

	private static final int DEFAULT_PROFILE_WIDTH = 512;

	public int id;
	public String name;
	public String surname;
	public String city;
	public String region;
	public String country;
	public String email;
	public String chipId;
	public int totalPoints;
	public int currentPoints;

	public int totalDistance;
	public int totalExpiredPoints;
	public int totalSuccessRate;
	public LastSyncTime lastSync;

	public Bitmap picture;
	public int refuelCount = 0;

	protected SFDB sfdb;
	protected Context ctx;

    private static OkHttpClient httpClient = new OkHttpClient();

	public User(Context ctx)
			throws UnknownUserException, ParseException, JSONException {

		this.ctx = ctx;
		sfdb = SFDB.getInstance(ctx);
		this.setUserInfo(ctx);
	}

	protected void setUserInfo(Context ctx) throws JSONException {
		id = sfdb.getUserID();

		JSONObject user = sfdb.queryUserData();
		if (user.has(TABLE.COLUMN.NAME)) name = user.getString(TABLE.COLUMN.NAME);
		if (user.has(TABLE.COLUMN.SURNAME)) surname = user.getString(TABLE.COLUMN.SURNAME);
		if (user.has(TABLE.COLUMN.CITY)) city = user.getString(TABLE.COLUMN.CITY);
		if (user.has(TABLE.COLUMN.REGION)) region = user.getString(TABLE.COLUMN.REGION);
        if (user.has(TABLE.COLUMN.COUNTRY)) country = user.getString(TABLE.COLUMN.COUNTRY);
		if (user.has(TABLE.COLUMN.EMAIL)) email = user.getString(TABLE.COLUMN.EMAIL);
		if (user.has(TABLE.COLUMN.CHIP_ID)) chipId = user.getString(TABLE.COLUMN.CHIP_ID);
		if (user.has(TABLE.COLUMN.TOTAL_POINTS)) totalPoints = user.getInt(TABLE.COLUMN.TOTAL_POINTS);
		if (user.has(TABLE.COLUMN.CURRENT_POINTS)) currentPoints = user.getInt(TABLE.COLUMN.CURRENT_POINTS);

		picture = getProfilePic();

		JSONObject profileData = sfdb.queryProfileData();
		if(profileData.has(GLOBALS.PARAM_KEY.TOTAL_DISTANCE)) {
			totalDistance = profileData.getInt(GLOBALS.PARAM_KEY.TOTAL_DISTANCE);
		}
		if(profileData.has(GLOBALS.PARAM_KEY.TOTAL_EXPIRED_POINTS)) {
			totalExpiredPoints = profileData.getInt(GLOBALS.PARAM_KEY.TOTAL_EXPIRED_POINTS);
		}
		if(profileData.has(GLOBALS.PARAM_KEY.TOTAL_SUCCESS_RATE)) {
			totalSuccessRate = profileData.getInt(GLOBALS.PARAM_KEY.TOTAL_SUCCESS_RATE);
		}

		lastSync = new LastSyncTime(ctx);
	}

	public static int authenticate(String email, String password)
			throws Exception {

		Map<String, String> postParams = new HashMap<>();
		postParams.put("email", email);
		postParams.put("password", password);

		ServerAPI request = new ServerAPI("authenticate");
		JSONObject result = request.sendRequest(postParams);
		if (request.responseCode != GLOBALS.HTTP_RESPONSE.OK) {
			switch (request.responseCode) {
				case GLOBALS.HTTP_RESPONSE.FORBIDDEN:
					if (!result.has(GLOBALS.ERROR_MSG_KEY))
						throw new IOException("Error message key not returned by the server");
					String errorMsg = result.getString(GLOBALS.ERROR_MSG_KEY);
					if (errorMsg.equals(GLOBALS.BAD_EMAIL))
						throw new UnknownUserException(GLOBALS.BAD_EMAIL);
					else if (errorMsg.equals(GLOBALS.BAD_PASS))
						throw new IncorrectPasswordException(GLOBALS.BAD_PASS);
					break;
				default:
					throw new Exception("Unknown error occurred.");
			}
		} else if (!result.has(GLOBALS.USER_ID)) {
			throw new IOException("Server has not responded with the user's ID");
		}

		return result.getInt(GLOBALS.USER_ID);
	}

	public static void saveProfilePicture(Context ctx, String URI) {
        try {
            Request request = new Request.Builder().url(URI).build();
            ResponseBody responseBody = httpClient.newCall(request).execute().body();
            InputStream is = responseBody.byteStream();
            Bitmap picture = BitmapFactory.decodeStream(new BufferedInputStream(is));

            if (picture != null) {
                File directory = new File(ctx.getCacheDir(), GLOBALS.DIR.PROFILE_PIC);
                File profilePicPath = new File(directory, GLOBALS.FILE.PROFILE_PIC);

                FileOutputStream fos;
                try {
                    directory.mkdirs();
                    fos = new FileOutputStream(profilePicPath);
                    picture = resizeProfilePicture(picture);
                    picture.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

    private static Bitmap resizeProfilePicture(Bitmap picture) {
        return resizeProfilePicture(picture, DEFAULT_PROFILE_WIDTH);
    }

    private static Bitmap resizeProfilePicture(Bitmap picture, int dimension) {
        int width = picture.getWidth();
        int height = picture.getHeight();
        float scale = (float) dimension / (width < height ? width : height);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                picture, (int) (width * scale), (int) (height * scale), false);
        picture.recycle();

        return resizedBitmap;
    }

	private Bitmap getProfilePic() {
		try {
            File directory = new File(ctx.getCacheDir(), GLOBALS.DIR.PROFILE_PIC);

			File f = new File(directory, GLOBALS.FILE.PROFILE_PIC);
			return f.exists() ? BitmapFactory.decodeStream(new FileInputStream(f)) : null;
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	public class LastSyncTime {

		private Date lastSyncDate = null;

		public LastSyncTime(Context ctx) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
			String lastSync = prefs.getString(GLOBALS.LAST_UPDATE, "NEVER");
			try {
				DateFormat formatter = SFDB.DATE_FORMAT;
				formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
				lastSyncDate = formatter.parse(lastSync);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		public int getTime() {
			long diff = (new Date().getTime()) - lastSyncDate.getTime();
			double diffHours = diff / (60 * 60 * 1000);

			return (int) Math.round(diffHours);
		}
	}

	public static final class TABLE {
		public static final String NAME = "users";

		public static final String CREATE =
				"CREATE TABLE " + NAME + " (" +
						COLUMN.ID + " INTEGER PRIMARY KEY," +
						COLUMN.NAME + " TEXT," +
						COLUMN.SURNAME + " TEXT," +
						COLUMN.CITY + " TEXT," +
						COLUMN.REGION + " TEXT," +
                        COLUMN.COUNTRY + " TEXT," +
						COLUMN.EMAIL + " TEXT," +
						COLUMN.CHIP_ID + " TEXT," +
						COLUMN.TOTAL_POINTS + " INTEGER," +
						COLUMN.CURRENT_POINTS + " INTEGER," +
						COLUMN.EDITED + " INTEGER DEFAULT 0" +
						");";

		public static final String DROP = "DROP TABLE IF EXISTS " + NAME;

		/**
		 * {@code edited} defaults to true
		 * @see TABLE#getContentValues(JSONObject, Boolean)
		 */
		public static ContentValues getContentValues(JSONObject data)
				throws JSONException {

			return getContentValues(data, true);
		}

		/**
		 * Returns the user's content values based on the json object and the edition preference.
		 *
		 * @param data contains the user's data
		 * @param edited editing preference, that marks the database row as edited
		 * @return the user's content values
		 * @throws JSONException if the json object could not be read properly
		 * @since 1.0
		 */
		public static ContentValues getContentValues(JSONObject data, Boolean edited)
				throws JSONException {

			ContentValues cv = new ContentValues();
			//must have
			cv.put(COLUMN.ID, data.getInt(COLUMN.ID));
			//optional
			if (data.has(COLUMN.NAME)) cv.put(COLUMN.NAME, data.getString(COLUMN.NAME));
			if (data.has(COLUMN.SURNAME)) cv.put(COLUMN.SURNAME, data.getString(COLUMN.SURNAME));
			if (data.has(COLUMN.CITY)) cv.put(COLUMN.CITY, data.getString(COLUMN.CITY));
			if (data.has(COLUMN.REGION)) cv.put(COLUMN.REGION, data.getString(COLUMN.REGION));
            if (data.has(COLUMN.COUNTRY)) cv.put(COLUMN.COUNTRY, data.getString(COLUMN.COUNTRY));
			if (data.has(COLUMN.EMAIL)) cv.put(COLUMN.EMAIL, data.getString(COLUMN.EMAIL));
			if (data.has(COLUMN.CHIP_ID)) cv.put(COLUMN.CHIP_ID, data.getString(COLUMN.CHIP_ID));
			if (data.has(COLUMN.TOTAL_POINTS)) cv.put(COLUMN.TOTAL_POINTS, data.getInt(COLUMN.TOTAL_POINTS));
			if (data.has(COLUMN.CURRENT_POINTS)) cv.put(COLUMN.CURRENT_POINTS, data.getInt(COLUMN.CURRENT_POINTS));
			cv.put(COLUMN.EDITED, edited ? 1 : 0);

			return cv;
		}

		public static final class COLUMN {
			public static final String ID = "id";
			public static final String NAME = "name";
			public static final String SURNAME = "surname";
			public static final String CITY = "city";
			public static final String REGION = "region";
            public static final String COUNTRY = "country";
			public static final String EMAIL = "email";
			public static final String CHIP_ID = "chip_card_id";
			public static final String TOTAL_POINTS = "total_points";
			public static final String CURRENT_POINTS = "current_points";
			public static final String EDITED = "edited";
		}
	}
}
