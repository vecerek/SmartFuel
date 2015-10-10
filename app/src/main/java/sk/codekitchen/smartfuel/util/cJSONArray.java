package sk.codekitchen.smartfuel.util;

import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Extends JSONArray class, so it is able to create JSONArray out of a database cursor.
 *
 * @author Attila Večerek
 * @since 1.0
 */
public class cJSONArray extends JSONArray {

	/**
	 * Constructs a JSONArray from a database cursor.
	 * @param cursor database cursor
	 */
	public cJSONArray(Cursor cursor) {
		if (cursor != null) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				this.put(new cJSONObject(cursor));
				cursor.moveToNext();
			}

			cursor.close();
		}
	}
}
