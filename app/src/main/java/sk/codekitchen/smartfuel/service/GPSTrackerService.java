package sk.codekitchen.smartfuel.service;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;

import sk.codekitchen.smartfuel.exception.PermissionDeniedException;
import sk.codekitchen.smartfuel.exception.UnknownUserException;
import sk.codekitchen.smartfuel.model.Ride;
import sk.codekitchen.smartfuel.util.ConnectionManager;
import sk.codekitchen.smartfuel.util.GLOBALS;
import sk.codekitchen.smartfuel.util.Units;

public class GPSTrackerService extends Service implements LocationListener {

    private Context mContext;

    protected boolean isGPSEnabled = false;
    protected boolean isNetworkEnabled = false;
    protected boolean canGetLocation = false;

    //protected Location location = null;
    protected Ride ride;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 25; // meters
    private static final long MIN_TIME_BW_UPDATES = 1000; // millisec
    private static final long NETWORK_CHECK_INTERVAL = 30 * 1000; //30 seconds

    protected LocationManager locationManager;

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    Messenger mActivityeMessenger = null;

    Handler networkHandler = new Handler();
    Timer mTimer = null;
    ConnectionManager cm;

    /**
     * Message type: register the activity's messenger for receiving responses
     * from Service. We assume only one activity can be registered at one time.
     */
    public static final int REGISTER = 1;

    /**
     * Message type: text sent Service->Activity
     */
    public static final int UPDATE_STATE = 2;

    /**
     * Message type: error message passed to activity
     */
    public static final int ERROR = 3;

    /**
     * Message type: debug communication state
     */
    public static final int DEBUG = 4;

    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Log.i("TEST_IPC", "Message getting handled");
            switch (msg.what) {
                case REGISTER:
                    Log.d("TEST_IPC", "Registered Activity's Messenger.");
                    mActivityeMessenger = msg.replyTo;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    void updateActivity(int msgCode, CharSequence text) {
        Log.i("TEST_IPC", "Updating activity");
        if (mActivityeMessenger == null) {
            Log.d("TEST_IPC", "Cannot send message to activity - no activity registered to this service.");
        } else {
            Log.d("TEST_IPC", "Sending message to activity: " + text);
            Bundle data = new Bundle();
            data.putCharSequence("data", text);
            Message msg = Message.obtain(null, msgCode);
            msg.setData(data);

            try {
                mActivityeMessenger.send(msg);
            } catch (RemoteException e) {
                // We always have to trap RemoteException
                // (DeadObjectException
                // is thrown if the target Handler no longer exists)
                e.printStackTrace();
            }
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.i("TEST_IPC", "onBind triggered");
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("TEST_IPC", "onUnbind triggered");
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i("TEST_IPC", "onRebind triggered");
        super.onRebind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("TEST_IPC", "onStartCommand triggered");
        this.mContext = getApplicationContext();
        cm = new ConnectionManager(mContext);

        try {
            Log.i("TEST_IPC", "Creating Ride object");
            this.ride = new Ride(mContext);

            if (mTimer != null) {
                mTimer.cancel();
            } else {
                mTimer = new Timer();
            }
            mTimer.scheduleAtFixedRate(new checkNetworkConnectionTimerTask(), 0, NETWORK_CHECK_INTERVAL);

            Log.i("TEST_IPC", "Location record is being added");
            this.ride.addRecord(getLocation());

        } catch (PermissionDeniedException e) {
            e.printStackTrace();
            //the app can't work without the permission, user should be informed
        } catch (UnknownUserException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.i("TEST_IPC", "returning from onStartCommand");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mTimer.cancel();
        Log.i("TEST_IPC", "onDestroy triggered");
        locationManager.removeUpdates(GPSTrackerService.this);

		/*try {
            ride.saveActivity();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}*/
    }

    public Location getLocation() throws PermissionDeniedException {
        Location location = null;
        try {
            Log.i("TEST_IPC", "locationManager being created");
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            Log.i("TEST_IPC", "check if gps provider enabled");
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Log.i("TEST_IPC", "check if network provider enabled");
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.i("TEST_CONNECTION", "Network enabled: " + Boolean.toString(isNetworkEnabled));

            if (!isGPSEnabled) {
                showSettingsAlert();
            } else {
                this.canGetLocation = true;
                Log.i("TEST_IPC", "requesting location updates");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null) {
                    Log.i("TEST_IPC", "getting location");
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        updateActivity(DEBUG, getCurrentStateMessage(location));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle("GPS is off");
        alertDialog.setMessage("Do you want to turn it on?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("TEST_LOC", "onLocationChanged");
        if (location != null) {
            Log.i("TEST_LOC", "location not null");
            Log.i("TEST_LOC_LAT", Double.toString(location.getLatitude()));
            Log.i("TEST_LOC_LONG", Double.toString(location.getLongitude()));
            updateActivity(DEBUG, getCurrentStateMessage(location));
        } else {
            Log.d("TEST_IPC", "location is null");
        }
        ride.addRecord(location);
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (locationManager == null) {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        }

        locationManager.removeUpdates(GPSTrackerService.this);
        ride.resetLocations();
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (locationManager == null) {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            ride.addRecord(location);
        }

	}

	private String getCurrentStateMessage(Location location) {
		return String.format(
                "%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                GLOBALS.IPC_MESSAGE_KEY.SPEED,
                Float.toString(Units.getPreferredSpeed(location.getSpeed(), ride.isMph())),
                GLOBALS.IPC_MESSAGE_KEY.PROGRESS,
                Integer.toString(ride.getPercentage()),
                GLOBALS.IPC_MESSAGE_KEY.LIMIT,
                Integer.toString(Units.getPreferredSpeedLimit(ride.getSpeedLimit(), ride.isMph())),
                GLOBALS.IPC_MESSAGE_KEY.POINTS,
                Integer.toString(ride.getPoints()),
                GLOBALS.IPC_MESSAGE_KEY.DIST,
                Float.toString(Units.getPreferredDistance(ride.getTotalDistance(), ride.isMph()))
        );
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

    private class checkNetworkConnectionTimerTask extends TimerTask {

        private int mCounter = 1;

        @Override
        public void run() {
            networkHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i("TEST_CONNECTION", "Checking internet connection ("+Integer.toString(mCounter)+")");
                    (new checkNetworkConnectionTask()).execute((Void) null);
                    Log.i("TEST_CONNECTION", "Connection aborted is " + Boolean.toString(!ride.isConnection()));
                    mCounter++;
                }
            });
        }
    }

    private class checkNetworkConnectionTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            return cm.isNetworkOnline();
        }

        @Override
        protected void onPostExecute(Boolean isConnection) {
            if (!isConnection) {
                Log.i("TEST_CONNECTION", "No connection");
                ride.setAbortedConnection();
            }
            Log.i("TEST_CONNECTION", "Connection aborted is " + Boolean.toString(!ride.isConnection()));
        }
    }
}

