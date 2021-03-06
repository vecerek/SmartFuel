package sk.codekitchen.smartfuel.model;

import android.content.ContentValues;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import sk.codekitchen.smartfuel.exception.UnknownUserException;
import sk.codekitchen.smartfuel.util.GLOBALS;
import sk.codekitchen.smartfuel.util.GPXGenerator;
import sk.codekitchen.smartfuel.util.ReverseGeocoder;

/**
 * Class, that handles the activity recording.
 * Activities should be recorded every time due
 * to possible lack of connectivity to the internet.
 * @author Attila Večerek
 */
public class Ride {
	protected static final int POINTS_PER_KM = 1;
	protected static final float DISTANCE_TO_GET_POINTS = 1000f; //in meters
	protected static final float ROAD_INTERVAL = 50f; //in meters
	protected static final float HIGHWAY_INTERVAL = 200f; //in meters

	protected int speedLimit = 0; //in kmph
	protected String roadUse;
	protected Location curLoc = null;
	protected Location prevLoc = null;
	protected float nextSpeedLimitCall = ROAD_INTERVAL; //meters

	protected Vector<Location> locations;

	protected float totalDistance = 0f; //in meters
	protected float progressCounter = 0f; //cyclic distance counter in meters
	protected float correctDistance = 0f; //in meters
	protected float speedingDistance = 0f; //in meters
	protected int points;
	protected int userID;

	protected long id;
	protected Context ctx;
	protected SFDB sfdb;
	protected boolean isMph;

    /**
     * Indicates, whether the internet connection got aborted at any time during driving.
     */
	protected boolean connectionAborted = false;

    /**
     * State of current connection.
     */
    public static boolean CONNECTION = true;

	public Ride(Context ctx)
			throws ParseException, UnknownUserException {

		this.ctx = ctx;
		this.locations = new Vector<>();
		sfdb = SFDB.getInstance(ctx);
		userID = sfdb.getUserID();

		isMph = PreferenceManager.getDefaultSharedPreferences(ctx).
				getBoolean(GLOBALS.SETTINGS_IS_MPH, false);
	}

	public String toString() {
		return  "userID: " + Integer.toString(userID) + "\n" +
				"points: " + Integer.toString(points) + "\n" +
				"total distance: " + Float.toString(totalDistance) + "\n" +
				"correct distance: " + Float.toString(correctDistance) + "\n" +
				"speeding distance: " + Float.toString(speedingDistance) + "\n";
	}

    public boolean isMph() { return isMph; }
	public boolean connectionEverAborted() { return connectionAborted; }
	public void setAbortedConnection() { connectionAborted = true; }

	public void addRecord(Location location, boolean async) {
		Log.i("TEST_IPC", "adding location record");
        if (location != null) {
            locations.add(location);

            prevLoc = curLoc;
            curLoc = location;

            if (speedLimit == 0 || totalDistance >= nextSpeedLimitCall) {
                Log.i("TEST_IPC", "updating speed limit");
                updateSpeedLimit(location, async);
            }

            float distDiff = computeDistance();

            if (speedLimit != 0) {
                if (curLoc.getSpeed() * GLOBALS.CONST.MPS2KPH <= speedLimit) {
                    addCorrectDistance(distDiff);
                } else {
                    addSpeedingDistance(distDiff);
                }
            } else {
                addTotalDistance(distDiff);
            }
        }
	}

	public void resetLocations() { prevLoc = curLoc= null; }

	/**
	 * Returns the distance between the {@code prevLoc} and {@code curLoc}
	 *
	 * @return distance in meters
	 */
	protected float computeDistance() {
		if(prevLoc != null)
			return prevLoc.distanceTo(curLoc);

		return 0f;
	}

    /**
     * Returns the speed limit based on the user's preferred speed unit.
     *
     * The conversion rounds the speed limit returned in km/h to the nearest 5, that represents the
     * mph limit. It is not a 100% correct way but hey, what else can we do about it?
     * @return the speed limit
     */
	public int getSpeedLimit() { return speedLimit; }

	public int getPercentage() {
		return Math.round(100*(progressCounter/DISTANCE_TO_GET_POINTS));
	}

	public int getPoints() { return points; }

	public float getTotalDistance() { return totalDistance; }

	public void addTotalDistance(float dist) { totalDistance += dist; }

	public void addCorrectDistance(float dist) {
		this.addTotalDistance(dist);
		correctDistance += dist;
		progressCounter += dist;
		if(progressCounter >= DISTANCE_TO_GET_POINTS) {
			progressCounter -= DISTANCE_TO_GET_POINTS;
			points += POINTS_PER_KM;
		}
	}

	public void addSpeedingDistance(float dist) {
		this.addTotalDistance(dist);
		speedingDistance += dist;
		progressCounter = 0f;
	}

    /**
     * Saves the activity into the database, if the connection has not been aborted and also
     * on the internal storage as a .gpx file.
     *
     * @return AbsolutePath if the saved GPX file for checking purposes
     *
     * @throws JSONException                if a key has not been found
     * @throws ParserConfigurationException if the gpx file is invalid
     * @throws IOException                  if not enough disk space on internal memory
     * @throws TransformerException         if GPX could not be written into File
     */
	public String saveActivity()
			throws JSONException, ParserConfigurationException, IOException, TransformerException {

		GPXGenerator gpx = new GPXGenerator(ctx, locations);
		gpx.createXML();

        if (!connectionAborted) insertDBActivity();

		return connectionAborted ? gpx.saveAsPendingActivity(userID) : gpx.save(userID);
	}

	protected void insertDBActivity() throws JSONException {
		JSONObject activity = new JSONObject();
		activity.put(TABLE.COLUMN.CORRECT_DISTANCE, correctDistance);
		activity.put(TABLE.COLUMN.SPEEDING_DISTANCE, speedingDistance);
		activity.put(TABLE.COLUMN.POINTS, points);

		sfdb.saveData(TABLE.NAME, activity, SFDB.ORIGIN_LOCAL);
		//return sfdb.lastInsertedId();
	}

	/*protected long lazySave() throws JSONException {
		return insertDBActivity();
	}*/

    public void updateSpeedLimit(Location loc, boolean async) {
        if (async) {
            (new updateSpeedLimitTask()).execute(loc);
        } else {
            updateSpeedLimit(loc);
        }
    }

    protected void updateSpeedLimit(Location loc) {
        ReverseGeocoder rgc = new ReverseGeocoder().setPosition(loc);

        try {
            speedLimit = rgc.getSpeedLimit(isMph);
        } catch (Exception e ) {
            setAbortedConnection();
            speedLimit = 0;
        }

        try {
            roadUse = rgc.getRoadUse();
            nextSpeedLimitCall += roadUse.equals("LimitedAccess")
                    ? HIGHWAY_INTERVAL : ROAD_INTERVAL;
        } catch (Exception e ) { setAbortedConnection(); }
    }

    private static List<File> getListFiles(File parentDir) { return getListFiles(parentDir, ".gpx"); }
    private static List<File> getListFiles(File parentDir, String ext) {
        List<File> inFiles = new ArrayList<>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                if(file.getName().endsWith(ext)){
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }

    public static List<File> getDrivingActivities(Context ctx, int userID) {
        File drivingActivitiesDir =
                new File(ctx.getFilesDir(), GPXGenerator.getActivitiesDir(userID));

        if (drivingActivitiesDir.exists()) {
            return getListFiles(drivingActivitiesDir);
        }

        return null;
    }

    public static void cleanDrivingActivities(Context ctx, int userID) {
        File drivingActivitiesDir =
                new File(ctx.getFilesDir(), GPXGenerator.getActivitiesDir(userID));

        if (drivingActivitiesDir.exists() && drivingActivitiesDir.isDirectory()) {
            try {
                FileUtils.cleanDirectory(drivingActivitiesDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

	public static void evaluatePendingActivities(Context ctx, int userID)
			throws ParseException, UnknownUserException,
			ParserConfigurationException, SAXException, IOException,
			JSONException {

		File pendingActivitiesDir =
                new File(ctx.getFilesDir(), GPXGenerator.getPendingDir(userID));

		if (pendingActivitiesDir.exists()) {
			File[] dirFiles = pendingActivitiesDir.listFiles();
			Ride roadActivity;
			for (File pending : dirFiles) {
				roadActivity  = new Ride(ctx);
				GPXGenerator gpx = new GPXGenerator(ctx, pending);
				Vector<Location> locations = gpx.getLocations();
				for (Location loc : locations) {
					roadActivity.addRecord(loc, false);
				}
				//save as lazy evaluated activity
				roadActivity.insertDBActivity();
				//renames and moves file to gpx routes directory
				boolean result = pending.renameTo(
						new File(
                                new File(ctx.getFilesDir(), GPXGenerator.getActivitiesDir(userID)),
								gpx.getFileName()
						)
				);
				if (!result)
					throw new IOException("File couldn't be moved");
			}
		}
	}

    private class updateSpeedLimitTask extends AsyncTask<Location, Void, Void> {
        @Override
        protected Void doInBackground(Location... loc) {
            updateSpeedLimit(loc[0]);
            return null;
        }
    }

	public static final class TABLE {

		public static final String NAME = "activities";

		public static final String CREATE =
				"CREATE TABLE " + NAME + " (" +
						COLUMN.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
						COLUMN.CORRECT_DISTANCE + " REAL," +
						COLUMN.SPEEDING_DISTANCE + " REAL," +
						COLUMN.CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
						COLUMN.POINTS + " INTEGER," +
						COLUMN.EXPIRED + " INTEGER DEFAULT 0," +
						COLUMN.SPENT + " INTEGER DEFAULT 0," +
						COLUMN.SYNCHRONIZED + " INTEGER DEFAULT 0" +
						");";

		public static final String DROP = "DROP TABLE IF EXISTS " + NAME;


		/**
		 * {@code _synchronized} defaults to false
		 * @see TABLE#getContentValues(JSONObject, boolean)
		 */
		public static ContentValues getContentValues(JSONObject data)
				throws JSONException {

			return getContentValues(data, false);
		}

		/**
		 * Returns the user activity's content values based on the json object and the synchronization preference.
		 *
		 * @param data contains the user activity's data
		 * @param _synchronized synchronization preference, that marks the database row as synchronized
		 *                      with the server's DB
		 * @return the user activity's content values
		 * @throws JSONException if the json object could not be read properly
		 * @since 1.0
		 */
		public static ContentValues getContentValues(JSONObject data, boolean _synchronized)
				throws JSONException {

			ContentValues cv = new ContentValues();
			if (data.has(COLUMN.ID)) cv.put(TABLE.COLUMN.ID, data.getInt(COLUMN.ID));
			if (data.has(COLUMN.CORRECT_DISTANCE)) cv.put(COLUMN.CORRECT_DISTANCE, data.getDouble(COLUMN.CORRECT_DISTANCE));
			if (data.has(COLUMN.SPEEDING_DISTANCE)) cv.put(COLUMN.SPEEDING_DISTANCE, data.getDouble(COLUMN.SPEEDING_DISTANCE));
			if (data.has(COLUMN.CREATED_AT)) cv.put(COLUMN.CREATED_AT, data.getString(COLUMN.CREATED_AT));
			if (data.has(COLUMN.POINTS)) cv.put(COLUMN.POINTS, data.getInt(COLUMN.POINTS));
			if (data.has(COLUMN.EXPIRED)) cv.put(COLUMN.EXPIRED, data.getInt(COLUMN.EXPIRED));
			if (data.has(COLUMN.SPENT)) cv.put(COLUMN.SPENT, data.getInt(COLUMN.SPENT));
			cv.put(COLUMN.SYNCHRONIZED, _synchronized ? 1 : 0);

			return cv;
		}

		public static final class COLUMN {
			public static final String ID = "id";
			public static final String CORRECT_DISTANCE = "correct_dist";
			public static final String SPEEDING_DISTANCE = "speeding_dist";
			public static final String CREATED_AT = "created_at";
			public static final String POINTS = "points";
			public static final String EXPIRED = "expired";
			public static final String SPENT = "spent";
			public static final String SYNCHRONIZED  = "synchronized";
		}
	}

}
