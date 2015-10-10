package sk.codekitchen.smartfuel.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import sk.codekitchen.smartfuel.ui.GUI.*;
import sk.codekitchen.smartfuel.R;

/**
 * @author Gabriel Lehocky
 *
 * The activity that works with the actual data.
 * 2 view modes:
 *  - Progress of getting a point
 *  - Speedmeter
 * Other features:
 *  - showing maximal permitted speed
 *  - in case of overspeeding the texture changes to warning colors
 *
 */
public class RecorderActivity extends Activity implements View.OnClickListener {

    /**
     * isOverLimit: set to true in case of overspeeding
     * WARNING: after changing this variable please call the changeColorBySpeed() method
     *      to change colors of the view
     */
    private boolean isOverLimit = false;

    /**
     * speedOrPercent: change between the 2 view modes
     * false = percent
     * true = speed
     */
    private boolean speedOrPercent = false;
    /**
     * speedLimit: 0 if unknown or unlimited
     */
    private int speedLimit = 0;

    private MainMenu menu;

    private SemiboldTextView noGps;
    private SemiboldTextView noSignal;

    private ProgressBar progressBar;
    private LightTextView progressValue;
    private LightTextView progressSufix;
    private LightTextView progressComment;
    private SemiboldTextView progressCommentBold;
    private LinearLayout progressData;

    private int progressMax;
    private int progressPercent = 0;
    private int progressSpeed = 0;

    private LinearLayout btnSpeed;
    private LinearLayout btnPercent;
    private ImageView icoBtnPercent;
    private ImageView icoBtnSpeed;
    private LightTextView txtBtnPercent;
    private LightTextView txtBtnSpeed;

    private LinearLayout maxPermittedSign;
    private SemiboldTextView maxPermittedSpeed;

    private SemiboldTextView pointCurrent;
    private SemiboldTextView pointOverall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        menu = new MainMenu(this, MainMenu.RECORDER_ID);
        setView();
    }

    /**
     * initializes the View
     */
    private void setView(){
        // speed/percent switch
        btnSpeed = (LinearLayout) findViewById(R.id.btn_speed);
        btnPercent = (LinearLayout) findViewById(R.id.btn_percent);
        btnSpeed.setOnClickListener(this);
        btnPercent.setOnClickListener(this);
        icoBtnPercent = (ImageView) findViewById(R.id.icon_percent);
        icoBtnSpeed = (ImageView) findViewById(R.id.icon_speed);
        txtBtnPercent = (LightTextView) findViewById(R.id.txt_percent);
        txtBtnSpeed = (LightTextView) findViewById(R.id.txt_speed);

        // progressbar
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressValue = (LightTextView) findViewById(R.id.progress_value);
        progressSufix = (LightTextView) findViewById(R.id.progress_symbol);
        progressComment = (LightTextView) findViewById(R.id.progress_comment);
        progressCommentBold = (SemiboldTextView) findViewById(R.id.progress_comment_bold);
        progressData = (LinearLayout) findViewById(R.id.progress_central_data);

        noGps = (SemiboldTextView) findViewById(R.id.progress_no_gps);
        noSignal = (SemiboldTextView) findViewById(R.id.progress_no_signal);
        noGps.setOnClickListener(this);
        maxPermittedSpeed = (SemiboldTextView) findViewById(R.id.max_permitted_speed);
        maxPermittedSign =(LinearLayout) findViewById(R.id.max_permitted_sign);

        // points data
        pointCurrent = (SemiboldTextView) findViewById(R.id.actual_points);
        pointOverall = (SemiboldTextView) findViewById(R.id.overall_points);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_speed:
                if (!speedOrPercent)
                    changeColorsBySwitch();
                break;
            case R.id.btn_percent:
                if(speedOrPercent)
                    changeColorsBySwitch();
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
    private void changeColorsBySwitch() {
        speedOrPercent = !speedOrPercent;

        // change icon alphas
        float a = icoBtnPercent.getAlpha();
        icoBtnPercent.setAlpha(icoBtnSpeed.getAlpha());
        icoBtnSpeed.setAlpha(a);

        // change text colors
        int c = txtBtnPercent.getCurrentTextColor();
        txtBtnPercent.setTextColor(txtBtnSpeed.getCurrentTextColor());
        txtBtnSpeed.setTextColor(c);

        changeColorBySpeed();

        if (speedOrPercent) { // speed
            progressSufix.setText("");
            progressComment.setText("");
            progressCommentBold.setText(getString(R.string.rec_kmph));

            setSpeedLimit(speedLimit);

            // ---- TEST DATA ONLY --------------------------------------------------
            changeProgress(progressSpeed);
            // -------------------------------------------------------------------
        }
        else { // percent
            progressSufix.setText(getString(R.string.rec_percent_symbol));
            progressComment.setText(getString(R.string.rec_comment_1));
            progressCommentBold.setText(getString(R.string.rec_comment_2));

            changeProgressMax(100); // 100% is always max for ths view

            // ---- TEST DATA ONLY --------------------------------------------------
            changeProgress(progressPercent);
            // -------------------------------------------------------------------
        }
    }

    /**
     * Changes color scheme based on the isOverLimit and
     */
    public void changeColorBySpeed() {
        if (speedOrPercent) { // speed
            Utils.setBackgroundOfView(this, btnPercent, R.drawable.round_transparent);
            if (isOverLimit) {
                Utils.setBackgroundOfView(this, btnSpeed, R.drawable.round_bad_box_right);
                Utils.setProgressBarProgress(this, progressBar, R.drawable.progressbar_arch_grad_bad);
                progressValue.setTextColor(Colors.RED);
            } else {
                Utils.setBackgroundOfView(this, btnSpeed, R.drawable.round_highlight_box_right);
                Utils.setProgressBarProgress(this, progressBar, R.drawable.progressbar_arch_grad_good);
                progressValue.setTextColor(Colors.WHITE);
            }
        } else { // percent
            Utils.setBackgroundOfView(this, btnSpeed, R.drawable.round_transparent);
            if (isOverLimit) {
                Utils.setBackgroundOfView(this, btnPercent, R.drawable.round_bad_box_left);
                Utils.setProgressBarProgress(this, progressBar, R.drawable.progressbar_arch_grad_bad);
                progressSufix.setTextColor(Colors.RED);
                progressValue.setTextColor(Colors.ORANGE);

            }
            else {
                Utils.setBackgroundOfView(this, btnPercent, R.drawable.round_highlight_box_left);
                Utils.setProgressBarProgress(this, progressBar, R.drawable.progressbar_arch_grad_good);
                progressSufix.setTextColor(Colors.HIGHIGHT);
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

        if (speedOrPercent) { // speed
            if (progressMax <= progressSpeed){
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
    private void changeProgress(int val){
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
    public void setSpeedLimit(int l){
        if (l > 0) {
            maxPermittedSign.setVisibility(View.VISIBLE);
            maxPermittedSpeed.setText(String.valueOf(l));
            if (speedOrPercent) changeProgressMax(l);
        }
        else if (l == 0){
            maxPermittedSign.setVisibility(View.INVISIBLE);
        }
        speedLimit = l;
    }

    /**
     * changes layout based on gps provider
     * @param gps
     */
    public void isGPS(boolean gps){
        if (gps) {
            noGps.setVisibility(View.GONE);
            progressData.setVisibility(View.VISIBLE);
        }
        else {
            noGps.setVisibility(View.VISIBLE);
            progressData.setVisibility(View.GONE);
        }
    }

    /**
     * changes layout when no signal or signal is back
     * @param signal
     */
    public void isSignal(boolean signal){
        if (signal) {
            noSignal.setVisibility(View.GONE);
            progressData.setVisibility(View.VISIBLE);
        }
        else {
            noSignal.setVisibility(View.VISIBLE);
            progressData.setVisibility(View.GONE);
        }
    }

    /**
     * changes layout based on current speed
     * @param s
     */
    public void setSpeed(int s){
        progressSpeed = s;
        if (speedOrPercent) changeProgress(s);
        if (progressSpeed > speedLimit) {
            isOverLimit = true;
            changeColorBySpeed();
        }
    }

    /**
     * changes layout based on vurrent percents
     * @param p
     */
    public void setPercent(int p){
        progressPercent = p;
        if (!speedOrPercent) changeProgress(p);
    }

    /**
     * sets current points text
     * @param val
     */
    public void setCurrentPoints(int val){
        pointCurrent.setText(String.valueOf(val));
    }

    /**
     * sets overall points text
     * @param val
     */
    public void setOverallPoints(int val){
        pointOverall.setText(String.valueOf(val));
    }

    @Override
    public void onBackPressed() {

    }
}
