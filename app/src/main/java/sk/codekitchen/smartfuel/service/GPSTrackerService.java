package sk.codekitchen.smartfuel.service;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import java.text.ParseException;

import sk.codekitchen.smartfuel.exception.PermissionDeniedException;
import sk.codekitchen.smartfuel.exception.UnknownUserException;
import sk.codekitchen.smartfuel.model.Ride;
//import sk.codekitchen.smartfuel.ui.MainActivity;

public class GPSTrackerService extends Service implements LocationListener {

	protected static final int LOCATION_REQUEST_CODE = 1;
	private final Context mContext;

	//private MainActivity mainActivity;

	protected boolean isGPSEnabled = false;
	//protected boolean isNetworkEnabled = false;
	protected boolean canGetLocation = false;

	//protected Location location = null;
	protected Ride roadActivity;

	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 0 meters
	private static final long MIN_TIME_BW_UPDATES = 0; // 0 sec

	protected LocationManager locationManager;

	//Activity should have a copy in case of lost internet connection
	protected boolean networkLost = false;

	public GPSTrackerService(Context context/*, MainActivity main*/) {
		this.mContext = context;

		try {
			this.roadActivity = new Ride(context);
			this.roadActivity.addRecord(getLocation());

		} catch (PermissionDeniedException e) {
			e.printStackTrace();
			//the app can't work without the permission, user should be informed
		} catch (UnknownUserException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public Location getLocation() throws PermissionDeniedException {
		Location location = null;
		try {
			locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			//isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled) {
				showSettingsAlert();
			} else {
				this.canGetLocation = true;

				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
				if (locationManager != null) {
					location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					if (location != null) {
						//mainActivity.newGPSData(location.getLatitude(), location.getLongitude(), location.getSpeed(), location.getAccuracy(), distance, speedLimit);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	public void showSettingsAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

		alertDialog.setTitle("GPS is off");
		alertDialog.setMessage("Do you want to turn it on?");
		alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int which) {
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
		roadActivity.addRecord(location);
		//mainActivity.newGPSData(location.getLatitude(), location.getLongitude(), location.getSpeed(), location.getAccuracy(), distance, speedLimit);
	}

	@Override
	public void onProviderDisabled(String provider) {
		if(locationManager == null){
			locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
		}

		locationManager.removeUpdates(GPSTrackerService.this);
		roadActivity.resetLocations();
	}

	@Override
	public void onProviderEnabled(String provider) {
		if (locationManager == null) {
			locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
		}

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location != null) {
			roadActivity.addRecord(location);
			//mainActivity.newGPSData(location.getLatitude(), location.getLongitude(), location.getSpeed(), location.getAccuracy(), distance, speedLimit);
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}

