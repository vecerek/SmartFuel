package sk.codekitchen.smartfuel.ui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
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

		@Override
		protected Void doInBackground(Void... params) {
			try {
				user = new User(getApplicationContext());
				int tmp = user.lastSync.getTime();
				if (tmp >= 24) {
					lastSyncTime = String.valueOf(tmp/24) + " " +
							getText(R.string.profile_last_sync_days).toString();
				} else {
					lastSyncTime = String.valueOf(tmp) + " " +
							getText(R.string.profile_last_sync_hours).toString();
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
				profilePic.setImageBitmap(user.picture);
				fullName.setText(user.getFullname().toLowerCase());
				address.setText(user.getAddress().toLowerCase());
				totalDistance.setText(user.totalDistance);
				currentPoints.setText(user.currentPoints);
				successRate.setText(user.totalSuccessRate);
				totalPoints.setText(user.totalPoints);
				expiredPoints.setText(user.totalExpiredPoints);
				refuelCount.setText(user.refuelCount);
				lastSync.setText(lastSyncTime);
			}
		}
	}
}
