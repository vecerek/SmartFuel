package sk.codekitchen.smartfuel.model;

import android.content.ContentValues;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Attila Večerek
 */
public class Event {

	public static final class TABLE {
		public static final String NAME = "events";

		public static final String CREATE =
				"CREATE TABLE " + NAME + " (" +
						COLUMN.ID + " INTEGER PRIMARY KEY," +
						COLUMN.TYPE + " TEXT," +
						COLUMN.CREATED_AT + " TEXT," +
						COLUMN.SEEN + " INTEGER DEFAULT 0" +
						");";

		public static final String DROP = "DROP TABLE IF EXISTS " + NAME;

		/**
		 * Returns the event's content values based on the json object.
		 *
		 * @param data contains the event's data
		 * @return the event's content values
		 * @throws JSONException if the json object could not be read properly
		 * @since 1.0
		 */
		public static ContentValues getContentValues(JSONObject data)
				throws JSONException {
			ContentValues cv = new ContentValues();
			//must have
			cv.put(COLUMN.ID, data.getInt(COLUMN.ID));
			cv.put(COLUMN.TYPE, data.getString(COLUMN.TYPE));
			cv.put(COLUMN.CREATED_AT, data.getString(COLUMN.CREATED_AT));

			return cv;
		}

		public static final class COLUMN {
			public static final String ID = "id";
			public static final String TYPE = "type";
			public static final String CREATED_AT = "created_at";
			public static final String SEEN = "seen";
		}
	}

	public static final class CONTENT {

		public static final class TABLE {
			public static final String NAME = "events_content";

			public static final String CREATE =
					"CREATE TABLE " + NAME + " (" +
							COLUMN.ID + " INTEGER PRIMARY KEY," +
							COLUMN.EVENT_ID + " INTEGER," +
							COLUMN.LANGUAGE + " TEXT," +
							COLUMN.KEY + " TEXT," +
							COLUMN.VALUE + " TEXT," +
							"FOREIGN KEY("+COLUMN.EVENT_ID+") " +
							"REFERENCES "+Event.TABLE.NAME+"("+Event.TABLE.COLUMN.ID+")" +
							");";

			public static final String DROP = "DROP TABLE IF EXISTS " + NAME;

			public static final String CREATE_INDEX =
					"CREATE INDEX " + COLUMN.EVENT_ID + "_index ON " + NAME + "("+COLUMN.EVENT_ID+")";


			/**
			 * Returns the event_content's content values based on the json object.
			 *
			 * @param data contains the event_content's data
			 * @return the event_content's content values
			 * @throws JSONException if the json object could not be read properly
			 * @since 1.0
			 */
			public static ContentValues getContentValues(JSONObject data)
					throws JSONException {
				ContentValues cv = new ContentValues();
				//must have
				cv.put(TABLE.COLUMN.ID, data.getInt(TABLE.COLUMN.ID));
				cv.put(TABLE.COLUMN.EVENT_ID, data.getInt(TABLE.COLUMN.EVENT_ID));
				cv.put(TABLE.COLUMN.LANGUAGE, data.getString(TABLE.COLUMN.LANGUAGE));
				cv.put(TABLE.COLUMN.KEY, data.getString(TABLE.COLUMN.KEY));
				cv.put(TABLE.COLUMN.VALUE, data.getString(TABLE.COLUMN.VALUE));

				return cv;
			}

			public static final class COLUMN {
				public static final String ID = "id";
				public static final String EVENT_ID = "event_id";
				public static final String LANGUAGE = "lang_id";
				public static final String KEY = "content_key";
				public static final String VALUE = "content_value";
			}
		}
	}

}
