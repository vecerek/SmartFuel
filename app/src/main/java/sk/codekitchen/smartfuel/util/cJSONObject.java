package sk.codekitchen.smartfuel.util;

import android.database.Cursor;
import android.util.Log;

import org.json.JSONObject;

/**
 * Extends JSONObject class, so it is able to create JSONObject out of a database cursor.
 *
 * @author Attila Večerek
 * @since 1.0
 */
public class cJSONObject extends JSONObject {

	/**
	 * Constructs a JSONObject from a database cursor.
	 * @param cursor database cursor
	 */
	public cJSONObject(Cursor cursor) {
		int colCnt = cursor.getColumnCount();

		for (int i = 0; i < colCnt; i++) {
			String col;
			if ((col = cursor.getColumnName(i)) != null) {
				try {
					if (!cursor.isNull(i)) {
						this.put(col, cursor.getString(i));
					}

				} catch (Exception e) {
					Log.d("Column: " + col, e.getMessage());
				}
			}
		}
	}
}
