package sk.codekitchen.smartfuel.model;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;

import sk.codekitchen.smartfuel.exception.UnknownUserException;

/**
 * Retrieves and handles the user related data.
 * Both profile and statistics data.
 *
 * @author Attila Večerek
 */
public class User {

	public String name;
	public String surname;
	public String email;
	public String chipId;
	public int totalPoints;
	public int currentPoints;

	//public String city;
	//public String region;
	//public int totalDistance;
	//public int totalPoints;
	//public int successRate;

	protected Bitmap picture;
	protected boolean pictureLoaded = false;

	protected SFDB sfdb;

	public User(Context ctx)
			throws UnknownUserException, ParseException, JSONException {

		sfdb = new SFDB(ctx);
		this.setUserInfo();
	}

	public int id() { return sfdb.getUserID(); }

	public boolean isPictureLoaded() {
		return pictureLoaded;
	}

	public Bitmap getPicture() {
		return pictureLoaded ? picture : null;
	}

	protected void setUserInfo() throws JSONException {
		JSONObject user = sfdb.queryUserData();
		name = user.getString(TABLE.COLUMN.NAME);
		surname = user.getString(TABLE.COLUMN.SURNAME);
		email = user.getString(TABLE.COLUMN.EMAIL);
		chipId = user.getString(TABLE.COLUMN.CHIP_ID);
		totalPoints = user.getInt(TABLE.COLUMN.TOTAL_POINTS);
		currentPoints = user.getInt(TABLE.COLUMN.CURRENT_POINTS);

		// TODO: implement in the database a column for image url, street address and city
		// TODO: implement a method for getting the overall success rate out of the road activities table
	}

	public JSONObject getStats() throws JSONException {
		return sfdb.queryStats();
	}

	private class LoadImage extends AsyncTask<String, String, Bitmap> {
		// TODO: reimplement the way the application handles and retrieves user's profile picture

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//do something before execution
		}

		protected Bitmap doInBackground(String... args) {
			try {
				picture = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());

			} catch (Exception e) {
				e.printStackTrace();
			}

			return picture;
		}

		protected void onPostExecute(Bitmap picture) {
			//do something here with the picture
			pictureLoaded = true;
		}
	}

	public static class TABLE {
		public static final String NAME = "users";

		public static final String CREATE =
				"CREATE TABLE " + NAME + " (" +
						COLUMN.ID + " INTEGER PRIMARY KEY," +
						COLUMN.NAME + " TEXT," +
						COLUMN.SURNAME + " TEXT," +
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
			if (data.has(COLUMN.EMAIL)) cv.put(COLUMN.EMAIL, data.getString(COLUMN.EMAIL));
			if (data.has(COLUMN.CHIP_ID)) cv.put(COLUMN.CHIP_ID, data.getString(COLUMN.CHIP_ID));
			if (data.has(COLUMN.TOTAL_POINTS)) cv.put(COLUMN.TOTAL_POINTS, data.getInt(COLUMN.TOTAL_POINTS));
			if (data.has(COLUMN.CURRENT_POINTS)) cv.put(COLUMN.CURRENT_POINTS, data.getInt(COLUMN.CURRENT_POINTS));
			cv.put(COLUMN.EDITED, edited ? 1 : 0);

			return cv;
		}

		public static class COLUMN {
			public static final String ID = "id";
			public static final String NAME = "name";
			public static final String SURNAME = "surname";
			public static final String EMAIL = "email";
			public static final String CHIP_ID = "chip_card_id";
			public static final String TOTAL_POINTS = "total_points";
			public static final String CURRENT_POINTS = "current_points";
			public static final String EDITED = "edited";
		}
	}

	public static class STATS {

		public static class VIEW {
			public static final String NAME = "statistics";

			/**
			 * (strftime('%j', date(MyDate, '-3 days', 'weekday 4')) - 1) / 7 + 1 represent the ISOWeekNumber
			 *
			 * @see <a href="http://stackoverflow.com/questions/15082584/sqlite-return-wrong-week-number-for-2013#answer-15511864">Stackoverflow sqlite week number issue</a>
			 */
			public static final String CREATE =
					"CREATE VIEW " + NAME + " AS " +
							"SELECT SUM(points) as " + COLUMN.POINTS + "," +
							"ROUND(SUM(correct_dist), 1) as " + COLUMN.CORRECT_DISTANCE + "," +
							"ROUND(SUM(speeding_dist), 1) as " + COLUMN.SPEEDING_DISTANCE + "," +
							"SUM((points - spent)*expired) as " + COLUMN.TOTAL_EXPIRED + "," +
							"strftime('%u', created_at) as " + COLUMN.DAY + ", " + //returns days of week (1-7) starting with Monday
							"NULL as " + COLUMN.WEEK + "," +
							"NULL as " + COLUMN.MONTH + "\n" +
							"FROM `" + SmartFuelActivity.TABLE.NAME + "`\n" +
							"WHERE date(" + SmartFuelActivity.TABLE.COLUMN.CREATED_AT + ") BETWEEN date('now', 'weekday 1', '-7 days') AND date('now', 'weekday 1', '-1 day')\n" +
							"GROUP BY " + COLUMN.DAY + "\n" +

							"UNION ALL\n" +

							"SELECT SUM(points) as " + COLUMN.POINTS + "," +
							"ROUND(SUM(correct_dist), 1) as " + COLUMN.CORRECT_DISTANCE + "," +
							"ROUND(SUM(speeding_dist), 1) as " + COLUMN.SPEEDING_DISTANCE + "," +
							"SUM((points - spent)*expired) as " + COLUMN.TOTAL_EXPIRED + "," +
							"NULL as " + COLUMN.DAY + ", " +
							"(strftime('%j', date(created_at, '-3 days', 'weekday 4')) - 1) / 7 + 1 as " + COLUMN.WEEK + "," + //returns the ISOWeekNumbers
							"NULL as " + COLUMN.MONTH + "\n" +
							"FROM `" + SmartFuelActivity.TABLE.NAME + "`\n" +
							"WHERE date(" + SmartFuelActivity.TABLE.COLUMN.CREATED_AT + ") BETWEEN date('now', 'start of month') AND date('now', 'start of month', '+1 month', '-1 day')\n" +
							"GROUP BY " + COLUMN.WEEK + "\n" +

							"UNION ALL\n" +

							"SELECT SUM(points) as " + COLUMN.POINTS + "," +
							"ROUND(SUM(correct_dist), 1) as " + COLUMN.CORRECT_DISTANCE + "," +
							"ROUND(SUM(speeding_dist), 1) as " + COLUMN.SPEEDING_DISTANCE + "," +
							"SUM((points - spent)*expired) as " + COLUMN.TOTAL_EXPIRED + "," +
							"NULL as " + COLUMN.DAY + ", " +
							"NULL as " + COLUMN.WEEK + "," +
							"strftime('%m', created_at) as " + COLUMN.MONTH + "\n" + //returns the months (01-12)
							"FROM `" + SmartFuelActivity.TABLE.NAME + "`\n" +
							"WHERE date(" + SmartFuelActivity.TABLE.COLUMN.CREATED_AT + ") BETWEEN date('now', 'start of year') AND date('now', 'start of year', '+1 year', '-1 day')\n" +
							"GROUP BY " + COLUMN.MONTH;

			public static final String DROP = "DROP VIEW IF EXISTS " + NAME;

			public static class COLUMN {
				public static final String POINTS = SmartFuelActivity.TABLE.COLUMN.POINTS;
				public static final String CORRECT_DISTANCE = SmartFuelActivity.TABLE.COLUMN.CORRECT_DISTANCE;
				public static final String SPEEDING_DISTANCE = SmartFuelActivity.TABLE.COLUMN.SPEEDING_DISTANCE;
				public static final String TOTAL_EXPIRED = "total_expired";
				public static final String DAY = "day";
				public static final String WEEK = "week";
				public static final String MONTH = "month";
			}
		}
	}
}
