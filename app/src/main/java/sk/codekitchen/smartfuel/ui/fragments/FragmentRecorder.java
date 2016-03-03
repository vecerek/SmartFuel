package sk.codekitchen.smartfuel.ui.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.math.RoundingMode;

import sk.codekitchen.smartfuel.R;
import sk.codekitchen.smartfuel.ui.views.Colors;
import sk.codekitchen.smartfuel.ui.views.LightTextView;
import sk.codekitchen.smartfuel.ui.views.SemiboldTextView;
import sk.codekitchen.smartfuel.ui.views.Utils;
import sk.codekitchen.smartfuel.util.Formatter;
import sk.codekitchen.smartfuel.util.GLOBALS;
import sk.codekitchen.smartfuel.util.Units;

/**
 * @author Gabriel Lehocky
 */
public class FragmentRecorder extends Fragment implements View.OnClickListener {

    /**
     * isOverLimit: set to true in case of overspeeding
     * WARNING: after changing this variable please call the changeColorBySpeed() method
     *      to change colors of the view
     */
    private boolean isOverLimit = false;

    /**
     * isSetToSpeed: change between the 2 view modes
     * false = percent
     * true = speed
     */
    private boolean isSetToSpeed = false;

    /**
     * speedLimit: 0 if unknown or unlimited
     */
    private int speedLimit = 0;

    private RelativeLayout progressLayout;
    private RelativeLayout noGpsLayout;
    private RelativeLayout noDataLayout;

    private ProgressBar progressBar;
    private LightTextView progressValue;
    private LightTextView progressSuffix;
    private LightTextView progressComment;
    private SemiboldTextView progressCommentBold;
    private LinearLayout progressData;

    private int progressMax = 133;
    private int progressPercent = 0;
    private int progressSpeed = 0;

    private LinearLayout btnSpeed;
    private LinearLayout btnPercent;
    private LinearLayout btnChange;
    private ImageView icoBtnPercent;
    private ImageView icoBtnSpeed;
    private LightTextView txtBtnPercent;
    private LightTextView txtBtnSpeed;

    private float switchSelectIco;
    private int switchSelectText;
    private float switchDeslectIco;
    private int switchDeselectText;

    private LinearLayout maxPermittedSign;
    private SemiboldTextView maxPermittedSpeed;

    private SemiboldTextView drivingPoints;
    private SemiboldTextView totalDistance;

    private SharedPreferences preferences;
    private boolean isMph = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recorder, container, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        isMph = preferences.getBoolean(GLOBALS.SETTINGS_IS_MPH, false);

        // speed/percent switch
        btnPercent = (LinearLayout) view.findViewById(R.id.btn_percent);
        btnSpeed = (LinearLayout) view.findViewById(R.id.btn_speed);
        btnChange = (LinearLayout) view.findViewById(R.id.switch_meter);
        btnChange.setOnClickListener(this);
        icoBtnPercent = (ImageView) view.findViewById(R.id.icon_percent);
        icoBtnSpeed = (ImageView) view.findViewById(R.id.icon_speed);
        txtBtnPercent = (LightTextView) view.findViewById(R.id.txt_percent);
        txtBtnSpeed = (LightTextView) view.findViewById(R.id.txt_speed);

        switchSelectIco = icoBtnPercent.getAlpha();
        switchDeslectIco = icoBtnSpeed.getAlpha();
        switchSelectText = txtBtnPercent.getCurrentTextColor();
        switchDeselectText = txtBtnSpeed.getCurrentTextColor();

        // progressbar
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setMax(progressMax);
        progressValue = (LightTextView) view.findViewById(R.id.progress_value);
        progressSuffix = (LightTextView) view.findViewById(R.id.progress_symbol);
        progressComment = (LightTextView) view.findViewById(R.id.progress_comment);
        progressCommentBold = (SemiboldTextView) view.findViewById(R.id.progress_comment_bold);
        progressData = (LinearLayout) view.findViewById(R.id.progress_central_data);

        progressLayout = (RelativeLayout) view.findViewById(R.id.progress_area);
        noGpsLayout = (RelativeLayout) view.findViewById(R.id.progress_no_gps);
        noDataLayout = (RelativeLayout) view.findViewById(R.id.progress_no_data);
        noGpsLayout.setOnClickListener(this);

        maxPermittedSpeed = (SemiboldTextView) view.findViewById(R.id.max_permitted_speed);
        maxPermittedSign =(LinearLayout) view.findViewById(R.id.max_permitted_sign);

        // points data
        drivingPoints = (SemiboldTextView) view.findViewById(R.id.actual_points);
        totalDistance = (SemiboldTextView) view.findViewById(R.id.overall_points);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.switch_meter:
                changeProgressBySwitch();
                break;
            case R.id.progress_no_gps:
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                this.startActivity(intent);
                break;
        }
    }

    /**
     * Called when the view has to change to the other view.
     * (from percent to speed or from speed to percent view)
     */
    private void changeProgressBySwitch() {
        isSetToSpeed = !isSetToSpeed;

        changeColorBySpeed();

        if (isSetToSpeed) { // speed
            progressSuffix.setText("");
            progressComment.setText("");
            if (isMph) {
                progressCommentBold.setText(getString(R.string.rec_mph));
            }
            else {
                progressCommentBold.setText(getString(R.string.rec_kmph));
            }

            setSpeedLimit(speedLimit);
            changeProgress(progressSpeed);
        }
        else { // percent
            progressSuffix.setText(getString(R.string.rec_percent_symbol));
            progressComment.setText(getString(R.string.rec_comment_1));
            progressCommentBold.setText(getString(R.string.rec_comment_2));

            changeProgressMax(100); // 100% is always max for ths view
            changeProgress(progressPercent);
        }
    }

    /**
     * Changes color scheme based on the isOverLimit and
     */
    public void changeColorBySpeed() {
        if (isSetToSpeed) { // speed
            icoBtnSpeed.setAlpha(switchSelectIco);
            icoBtnPercent.setAlpha(switchDeslectIco);
            txtBtnSpeed.setTextColor(switchSelectText);
            txtBtnPercent.setTextColor(switchDeselectText);
            Utils.setBackgroundOfView(getActivity(), btnPercent, R.drawable.round_transparent);
            if (isOverLimit) {
                Utils.setBackgroundOfView(getActivity(), btnSpeed, R.drawable.round_bad_box_right);
                Utils.setProgressBarProgress(getActivity(), progressBar, R.drawable.progressbar_arch_grad_bad);
                progressValue.setTextColor(Colors.RED);
            }
            else {
                Utils.setBackgroundOfView(getActivity(), btnSpeed, R.drawable.round_highlight_box_right);
                Utils.setProgressBarProgress(getActivity(), progressBar, R.drawable.progressbar_arch_grad_good);
                progressValue.setTextColor(Colors.WHITE);
            }
        }
        else { // percent
            icoBtnPercent.setAlpha(switchSelectIco);
            icoBtnSpeed.setAlpha(switchDeslectIco);
            txtBtnPercent.setTextColor(switchSelectText);
            txtBtnSpeed.setTextColor(switchDeselectText);
            Utils.setBackgroundOfView(getActivity(), btnSpeed, R.drawable.round_transparent);
            if (isOverLimit) {
                Utils.setBackgroundOfView(getActivity(), btnPercent, R.drawable.round_bad_box_left);
                Utils.setProgressBarProgress(getActivity(), progressBar, R.drawable.progressbar_arch_grad_bad);
                progressSuffix.setTextColor(Colors.RED);
                progressValue.setTextColor(Colors.ORANGE);
            }
            else {
                Utils.setBackgroundOfView(getActivity(), btnPercent, R.drawable.round_highlight_box_left);
                Utils.setProgressBarProgress(getActivity(), progressBar, R.drawable.progressbar_arch_grad_good);
                progressSuffix.setTextColor(Colors.MAIN);
                progressValue.setTextColor(Colors.WHITE);
            }
        }
    }

    /**
     * changes progressbar maximal value
     * by hacking it to make it an 270 degree arc instead of a full ring
     * @param max
     */
    private void changeProgressMax(int max) {
        progressMax = max;
        progressBar.setMax(progressMax + progressMax / 3);

        if (isSetToSpeed) { // speed
            if (progressSpeed <= progressMax){
                isOverLimit = false;
                changeColorBySpeed();
            }
            else {
                progressBar.setProgress(progressMax);
                isOverLimit = true;
                changeColorBySpeed();
            }
        }
    }

    /**
     * changes the Actual value of the progressbar and changes the value text also
     * @param val
     */
    private void changeProgress(int val) {
        Log.d("TEST_GRAPHICS", "val: " + Integer.toString(val));
        Log.d("TEST_GRAPHICS", "progressMax: " + Integer.toString(progressMax));
        Log.d("TEST_GRAPHICS", "progressPercent: " + Integer.toString(progressPercent));
        if (val < progressMax){
            progressBar.setProgress(val);
        }
        else {
            progressBar.setProgress(progressMax);
        }
        progressValue.setText(String.valueOf(val));
    }

    /**
     * changes the speedlimit sign and also the progressbar max in case of speed
     * @param l - limit
     */
    public void setSpeedLimit(int l) {
        if (l > 0) {
            maxPermittedSign.setVisibility(View.VISIBLE);
            maxPermittedSpeed.setText(String.valueOf(l));
            if (isSetToSpeed) changeProgressMax(l);
        }
        else if (l == 0){
            maxPermittedSign.setVisibility(View.INVISIBLE);
        }
        speedLimit = l;
    }

    /**
     * can change layout to no gps
     * @param gps
     */
    public void isGPS(boolean gps) {
        if (isSetToSpeed){
            if (gps) {
                noGpsLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            }
            else {
                noGpsLayout.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
            }
        }
    }

    /**
     * can change layout to no data notification
     * @param data
     */
    public void isData(boolean data) {
        if (!isSetToSpeed){
            if (data) {
                noDataLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            }
            else {
                noDataLayout.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
            }
        }
    }

    /**
     * changes layout based on current speed
     * @param s
     */
    public void setSpeed(int s) {
        progressSpeed = s;
        if (isSetToSpeed) changeProgress(s);
        if (progressSpeed > speedLimit) {
            isOverLimit = true;
            changeColorBySpeed();
        }
    }

    /**
     * changes layout based on current percents
     * @param p
     */
    public void setPercent(int p) {
        Log.d("TEST_GRAPHICS", "Setting percent");
        progressPercent = p;
        Log.d("TEST_GRAPHICS", "Changing progress: " + Boolean.toString(!isSetToSpeed));
        if (!isSetToSpeed) changeProgress(p);
    }

    /**
     * sets driving points text
     * @param val
     */
    public void setDrivingPoints(int val) {
        drivingPoints.setText(Formatter.format(val, ' ', RoundingMode.CEILING));
    }

    /**
     * sets total distance text
     * @param val distance in the preferred unit
     */
    public void setTotalDistance(float val) {
        String dist;
        String unit;

        if (val <= 1f) {
            if (isMph) {
                dist = Formatter.Distance.format(Units.Distance.Mi.toFt(val));
                unit = getString(R.string.unit_feet);
            } else {
                dist = Formatter.Distance.format(Units.Distance.Km.toMe(val));
                unit = getString(R.string.unit_meter);
            }
        } else {
            unit = isMph ? getString(R.string.unit_mile) : getString(R.string.unit_kilometer);
            if (val < 10f) {
                dist = Formatter.Distance.format(val, "#,###.##");
            } else if (val < 100f) {
                dist = Formatter.Distance.format(val, "#,###.#");
            } else {
                dist = Formatter.Distance.format(val);
            }
        }
        totalDistance.setText(dist + unit);
    }

}
