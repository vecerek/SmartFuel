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
import sk.codekitchen.smartfuel.model.SFDB;
import sk.codekitchen.smartfuel.util.ConnectionManager;
import sk.codekitchen.smartfuel.util.GLOBALS;
import sk.codekitchen.smartfuel.util.Units;

public class GPSTrackerService extends Service implements LocationListener {

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
     * Message type: informs the activity about having no data about the speed limit
     */
    public static final int DATA = 3;

    /**
     * Message type: informing the Activity about no internet or GPS connection
     */
    public static final int SIGNAL = 4;

    /**
     * Message type: debug communication state
     */
    public static final int DEBUG = 5;

    /**
     * Message type: error message passed to activity
     */
    public static final int ERROR = 6;

    public static final String ON = "on";
    public static final String OFF = "off";

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 25; // meters
    private static final long MIN_TIME_BW_UPDATES = 1000; // millisec
    private static final long NETWORK_CHECK_INTERVAL = 30 * 1000; //30 seconds

    private Context mContext;

    protected boolean isGPSEnabled = false;
    protected boolean isNetworkEnabled = false;
    protected boolean canGetLocation = false;
    protected boolean noData = false;

    //protected Location location = null;
    protected Ride ride;

    protected LocationManager locationManager;

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    Messenger mActivityeMessenger = null;

    Handler networkHandler = new Handler();
    Timer mTimer = null;
    ConnectionManager cm;

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
        if (mActivityeMessenger != null) {
            Bundle data = new Bundle();
            data.putCharSequence("data", text);
            Message msg = Message.obtain(null, msgCode);
            msg.setData(data);

            try {
                mActivityeMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.mContext = getApplicationContext();
        cm = new ConnectionManager(mContext);

        try {
            this.ride = new Ride(mContext);

            if (mTimer != null) {
                mTimer.cancel();
            } else {
                mTimer = new Timer();
            }
            mTimer.scheduleAtFixedRate(new checkNetworkConnectionTimerTask(), 0, NETWORK_CHECK_INTERVAL);

            this.ride.addRecord(getLocation(), true);

        } catch (PermissionDeniedException e) {
            e.printStackTrace();
            //the app can't work without the permission, user should be informed
        } catch (UnknownUserException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mTimer.cancel();
        locationManager.removeUpdates(GPSTrackerService.this);

		/*try {
            ride.saveActivity();
            if (!ride.connectionEverAborted()) SFDB.getInstance(getApplicationContext()).sync();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
    }

    public Location getLocation() throws PermissionDeniedException {
        Location location = null;
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled) {
                showSettingsAlert();
            } else {
                this.canGetLocation = true;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        updateActivity(UPDATE_STATE, getCurrentStateMessage(location));
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
        if (location != null) {
            if (ride.getSpeedLimit() == 0 && !noData) {
                //The system just received no data about speed limit
                noData = true;
                updateActivity(DATA, OFF);
            } else if (noData) {
                //The system received speed limit data again
                noData = false;
                updateActivity(DATA, ON);
            }

            updateActivity(UPDATE_STATE, getCurrentStateMessage(location));
            ride.addRecord(location, true);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (locationManager == null) {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        }

        locationManager.removeUpdates(GPSTrackerService.this);
        ride.resetLocations();
        updateActivity(SIGNAL, OFF);
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (locationManager == null) {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            ride.addRecord(location, true);
        }
        updateActivity(SIGNAL, ON);
	}

	private String getCurrentStateMessage(Location location) {
		return String.format(
                "%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
                GLOBALS.IPC_MESSAGE_KEY.SPEED,
                Float.toString(Units.getPreferredSpeed(location.getSpeed(), ride.isMph())),
                GLOBALS.IPC_MESSAGE_KEY.PROGRESS,
                Integer.toString(ride.getPercentage()),
                GLOBALS.IPC_MESSAGE_KEY.LIMIT,
                Integer.toString(ride.getSpeedLimit()),
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
                    Log.i("TEST_CONNECTION", "Connection aborted is " + Boolean.toString(ride.connectionEverAborted()));
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
                // if connection just gets aborted, update activity
                if (Ride.CONNECTION) updateActivity(SIGNAL, OFF);
                // if connection gets aborted for the first time, set flag
                if (!ride.connectionEverAborted()) ride.setAbortedConnection();
            } else {
                if (!Ride.CONNECTION) updateActivity(SIGNAL, ON);
            }
            Log.i("TEST_CONNECTION", "Connection aborted is " + Boolean.toString(ride.connectionEverAborted()));
        }
    }
}

