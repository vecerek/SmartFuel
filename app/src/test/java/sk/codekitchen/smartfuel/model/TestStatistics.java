package sk.codekitchen.smartfuel.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Iterator;

import sk.codekitchen.smartfuel.BuildConfig;
import sk.codekitchen.smartfuel.util.GLOBALS;
import sk.codekitchen.smartfuel.util.ServerAPI;

/**
 * @author Attila Večerek
 */
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricGradleTestRunner.class)
public class TestStatistics extends AndroidTestCase {

	private Context context;
	private int userId;

	@Before
	public void setUp() {
		context = RuntimeEnvironment.application.getApplicationContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (prefs.getInt(GLOBALS.USER_ID, -1) != 1) {
			userId = 1;
			prefs.edit().putInt(GLOBALS.USER_ID, userId).commit();
		}

		try {
			SFDB.getInstance(context).sync();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testStatisticsInstantiation() throws Throwable {
		Statistics stats = Statistics.getInstance(context);

		JSONObject server_json = new ServerAPI("test/statistics/" + userId).sendRequest(null);

		Iterator<?> keys = server_json.keys();
		Iterator<?> cols;
		String tabKey, key;
		JSONObject tab, col;
		int colNum;
		int tCorrectDistance, tSpeedingDistance, tPoints, tExpiredPoints;
		int correctDistance, speedingDistance, points, expiredPoints;
		Statistics.TabData tabData;

		while(keys.hasNext()) {
			colNum = tCorrectDistance = tSpeedingDistance = tPoints = tExpiredPoints = 0;

			tabKey = (String) keys.next();
			switch (tabKey) {
				case Statistics.TabData.WEEK:
					tabData = stats.week;
					break;
				case Statistics.TabData.MONTH:
					tabData = stats.month;
					break;
				case Statistics.TabData.YEAR:
					tabData = stats.year;
					break;
				default:
					tabData = null;
			}

			if (tabData != null) {
				tab = server_json.getJSONObject(tabKey);
				cols = tab.keys();
				while(cols.hasNext()) {
					key = (String) cols.next();
					if (!tab.isNull(key)) {
						col = tab.getJSONObject(key);
					} else {
						col = null;
					}
					tPoints += points = tabData.cols.get(colNum).points;
					tCorrectDistance += correctDistance = tabData.cols.get(colNum).correctDistance;
					tSpeedingDistance += speedingDistance = tabData.cols.get(colNum).speedingDistance;
					tExpiredPoints += expiredPoints = tabData.cols.get(colNum).expiredPoints;

					if (col == null) {
						assertEquals("Error: Values do not equal in " + key + " of " + tabKey,
								0, points);
						assertEquals("Error: Values do not equal in " + key + " of " + tabKey,
								0, correctDistance);
						assertEquals("Error: Values do not equal in " + key + " of " + tabKey,
								0, speedingDistance);
						assertEquals("Error: Values do not equal in " + key + " of " + tabKey,
								0, expiredPoints);
					} else {
						assertEquals("Error: Values do not equal in " + key + " of " + tabKey,
								col.getInt(Statistics.VIEW.COLUMN.POINTS), points);
						assertEquals("Error: Values do not equal in " + key + " of " + tabKey,
								col.getInt(Statistics.VIEW.COLUMN.CORRECT_DISTANCE), correctDistance);
						assertEquals("Error: Values do not equal in " + key + " of " + tabKey,
								col.getInt(Statistics.VIEW.COLUMN.SPEEDING_DISTANCE), speedingDistance);
						assertEquals("Error: Values do not equal in " + key + " of " + tabKey,
								col.getInt(Statistics.VIEW.COLUMN.TOTAL_EXPIRED), expiredPoints);
					}
					colNum++;
				}

				assertEquals("Error: the total value of points does not equal with the expected value",
						tPoints, tabData.points);
				assertEquals("Error: the total value of expired points does not equal with the expected value",
						tExpiredPoints, tabData.totalExpiredPoints);
				assertEquals("Error: the value of total distance does not equal with the expected value",
						tCorrectDistance + tSpeedingDistance, tabData.distance);
				if (tCorrectDistance + tSpeedingDistance == 0) {
					assertEquals("Error: the value of success rate does not equal with the expected value",
							0, tabData.successRate);
				} else {
					int sRate = (int) Math.round(
							(100 * tCorrectDistance/(double) (tCorrectDistance + tSpeedingDistance))
					);
					assertEquals("Error: the total value of points does not equal with the expected value",
							sRate , tabData.successRate);
				}
			}
		}
	}
}
