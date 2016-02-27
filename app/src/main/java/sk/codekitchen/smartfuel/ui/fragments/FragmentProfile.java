package sk.codekitchen.smartfuel.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sk.codekitchen.smartfuel.R;
import sk.codekitchen.smartfuel.model.User;
import sk.codekitchen.smartfuel.ui.views.LightTextView;
import sk.codekitchen.smartfuel.ui.views.RoundedImageView;
import sk.codekitchen.smartfuel.ui.views.SemiboldTextView;
import sk.codekitchen.smartfuel.util.GLOBALS;
import sk.codekitchen.smartfuel.util.Units;

/**
 * @author Gabriel Lehocky
 */
public class FragmentProfile extends Fragment {

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
    private LightTextView totalDistanceUnits;

    private SharedPreferences preferences;
    private boolean isMph;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        isMph = preferences.getBoolean(GLOBALS.SETTINGS_IS_MPH, false);

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        address = (LightTextView) view.findViewById(R.id.profile_address);
        profilePic = (RoundedImageView) view.findViewById(R.id.profile_picture);
        fullName = (SemiboldTextView) view.findViewById(R.id.profile_full_name);
        totalDistance = (SemiboldTextView) view.findViewById(R.id.profile_total_distance);
        currentPoints = (SemiboldTextView) view.findViewById(R.id.profile_current_points);
        successRate = (SemiboldTextView) view.findViewById(R.id.profile_success_rate);
        totalPoints = (SemiboldTextView) view.findViewById(R.id.profile_total_points);
        expiredPoints = (SemiboldTextView) view.findViewById(R.id.profile_expired_points);
        refuelCount = (SemiboldTextView) view.findViewById(R.id.profile_refueling_count);
        lastSync = (SemiboldTextView) view.findViewById(R.id.profile_last_sync);
        totalDistanceUnits = (LightTextView) view.findViewById(R.id.profile_total_distance_unit);

        (new InsertProfileDataTask()).execute((Void) null);

        return view;
    }

    public void loadUnits(){
        if (isMph) {
            totalDistanceUnits.setText(getString(R.string.profile_total_distance_mile));
        }
    }

    private class InsertProfileDataTask extends AsyncTask<Void, Void, Void> {

        private User user;
        private String lastSyncTime;
        int color = 0;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Context ctx = getActivity().getApplicationContext();
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
                totalDistance.setText(
                        String.valueOf(
                            isMph ? Units.Speed.toImperial(user.totalDistance) : user.totalDistance
                        )
                );
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