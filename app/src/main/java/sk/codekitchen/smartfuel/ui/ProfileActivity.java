package sk.codekitchen.smartfuel.ui;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;

import sk.codekitchen.smartfuel.model.User;
import sk.codekitchen.smartfuel.ui.GUI.*;
import sk.codekitchen.smartfuel.R;

public class ProfileActivity extends Activity implements View.OnClickListener{

    private MainMenu menu;

    private RoundedImageView profilePic;
	private SemiboldTextView fullName;
	private LightTextView address;
    private SemiboldTextView totalDistance;
    private SemiboldTextView currentPoints;
    private SemiboldTextView successRate;
	private SemiboldTextView totalPoints;
	private SemiboldTextView expiredPoints;
	private SemiboldTextView refuelCount;
	private SemiboldTextView lastSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        menu = new MainMenu(this, MainMenu.USER_ID);
        setView();
	    (new InsertProfileDataTask()).execute((Void) null);
    }

    private void setView() {
	    profilePic    = (RoundedImageView) findViewById(R.id.profile_picture);
	    fullName      = (SemiboldTextView) findViewById(R.id.profile_full_name);
	    address       = (LightTextView) findViewById(R.id.profile_address);
        totalDistance = (SemiboldTextView) findViewById(R.id.profile_total_distance);
        currentPoints = (SemiboldTextView) findViewById(R.id.profile_current_points);
        successRate   = (SemiboldTextView) findViewById(R.id.profile_success_rate);
		totalPoints   = (SemiboldTextView) findViewById(R.id.profile_total_points);
	    expiredPoints = (SemiboldTextView) findViewById(R.id.profile_expired_points);
	    refuelCount   = (SemiboldTextView) findViewById(R.id.profile_refueling_count);
	    lastSync      = (SemiboldTextView) findViewById(R.id.profile_last_sync);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onBackPressed() {
        menu.goToActivity(menu.RECORDER_ID, RecorderActivity.class);
    }

	private class InsertProfileDataTask extends AsyncTask<Void, Void, Void> {

		private User user;
		private String lastSyncTime;
		int color = 0;

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Context ctx = getApplicationContext();
				user = new User(ctx);
				int tmp = user.lastSync.getTime();
				lastSyncTime = getText(R.string.profile_last_sync_before_text).toString() + " ";
				if (tmp >= 24) {
					lastSyncTime += String.valueOf(tmp/24) +
							getText(R.string.profile_last_sync_days).toString();
					color = ContextCompat.getColor(ctx, R.color.RED);
				} else {
					lastSyncTime += String.valueOf(tmp) +
							getText(R.string.profile_last_sync_hours).toString();
					color = ContextCompat.getColor(ctx, R.color.GREEN);
				}

			} catch (Exception e) {
				e.printStackTrace();
				user = null;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void param) {
			if (user != null) {
				if (user.picture != null) {
					profilePic.setImageBitmap(user.picture);
				}
				fullName.setText(getFullNameFormatted(user.name, user.surname));
				address.setText(getFullAddressFormatted(user.city, user.region));
				totalDistance.setText(String.valueOf(user.totalDistance));
				currentPoints.setText(String.valueOf(user.currentPoints));
				successRate.setText(getPercentageValue(user.totalSuccessRate));
				totalPoints.setText(String.valueOf(user.totalPoints));
				expiredPoints.setText(String.valueOf(user.totalExpiredPoints));
				refuelCount.setText(String.valueOf(user.refuelCount));
				lastSync.setText(lastSyncTime);
				lastSync.setTextColor(color);
			}
		}

		private String getPercentageValue(int value) { return String.valueOf(value) + "%"; }

		private String getFullAddressFormatted(String city, String region) {
			return toUpperCaseFirst(city) + ", " + toUpperCaseFirst(region);
		}

		private String getFullNameFormatted(String name, String surname) {
			return toUpperCaseFirst(name) + " " + toUpperCaseFirst(surname);
		}

		private String toUpperCaseFirst(String str) {
			str = str.toLowerCase();
			return Character.toUpperCase(str.charAt(0)) + str.substring(1);
		}
	}
}
