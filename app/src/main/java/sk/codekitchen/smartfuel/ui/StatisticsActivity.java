package sk.codekitchen.smartfuel.ui;

import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;

import sk.codekitchen.smartfuel.ui.GUI.*;
import sk.codekitchen.smartfuel.R;

/**
 * @author Gabriel Lehocky
 *
 * Activity that shows the users statistic data
 */
public class StatisticsActivity extends Activity implements View.OnClickListener, OnEntryClickListener {

    private MainMenu menu;

    // static data range values
    private final static int RANGE_WEEK = 1;
    private final static int RANGE_MONTH = 2;
    private final static int RANGE_YEAR = 3;
    private int range = RANGE_WEEK;

    // range setup clickable texts
    private LightTextView rangeWeek;
    private LightTextView rangeMonth;
    private LightTextView rangeYear;

    // swither between value types
    private boolean isPositive = true; // true - positive | false - negative
    private LightTextView switchPos;
    private LightTextView switchNeg;

    // information text views
    private SemiboldTextView infoDistance;
    private SemiboldTextView infoPoints;
    private SemiboldTextView infoSuccess;

    // line chart data
    private final static int CHART_VALUE_STEP = 5;
    private LineChartView lineChart;
    private LineSet dataSet;
    private LightTextView chartDot;
    private int selectedChartColumn = 0;
    private int lastInactiveColumn = 8; // default is week view
    private int chartMaxValue;
    private String[] chartLabels;
    private float[] chartValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        menu = new MainMenu(this, MainMenu.STATISTICS_ID);
        setView();

        updateChartData();
    }

    /**
     * Creates the default view of the activity and fills default values
     */
    private void setView(){
        // range settings
        rangeWeek = (LightTextView) findViewById(R.id.stat_week);
        rangeWeek.setOnClickListener(this);
        rangeMonth = (LightTextView) findViewById(R.id.stat_month);
        rangeMonth.setOnClickListener(this);
        rangeYear = (LightTextView) findViewById(R.id.stat_year);
        rangeYear.setOnClickListener(this);

        // positive / negative
        switchPos = (LightTextView) findViewById(R.id.btn_positive);
        switchPos.setOnClickListener(this);
        switchNeg = (LightTextView) findViewById(R.id.btn_negative);
        switchNeg.setOnClickListener(this);

        // informations
        infoDistance = (SemiboldTextView) findViewById(R.id.stat_km);
        infoPoints = (SemiboldTextView) findViewById(R.id.stat_points);
        infoSuccess = (SemiboldTextView) findViewById(R.id.stat_success);

        //chart
        Paint linePaint = new Paint();
        linePaint.setColor(Colors.GRAY);
        linePaint.setAlpha(255);

        lineChart = (LineChartView) findViewById(R.id.line_chart);
        lineChart.setOnClickListener(this);
        lineChart.setOnEntryClickListener(this);
        lineChart.setXLabels(AxisController.LabelPosition.OUTSIDE)
                .setYLabels(AxisController.LabelPosition.INSIDE)
                .setLabelsColor(Colors.GRAY)
                .setFontSize(32)
                .setTypeface(Typeface.createFromAsset(getAssets(), "Fonts/ProximaNova-Light.otf"))
                .setXAxis(false)
                .setYAxis(false)
                .setAxisThickness(1f)
                .setBorderSpacing(0f)
                .setTopSpacing(10f)
                .setAxisColor(Colors.GRAY);

        chartDot = (LightTextView) findViewById(R.id.chart_dot);
    }

    /**
     * Updates data in the chart and puts them into
     * arrays (chartLabels and chartValues) then calls
     * updateChartView()
     *
     * TODO:- call async task here to update data
            - download data based on "range" and "isPositive"
            - if no internet show local saved values,
              if is net, sync data and show loading spinner like at login
     */
    private void updateChartData(){
        removeDataPoint();

        /**
         * Always leave firs and last column label empty with some value
         */
        String[] lab = {"", "PO", "UT", "ST", "ST", "PI", "SO", "NE", ""};
        float[] val = {0f, 0f, 0f, 32f, 20f, 53f, 38f, 12f, 11f};

        chartLabels = lab;
        chartValues = val;

        // show data in the chart
        updateChartView();
    }

    /**
     * Fills chart view with the actual data
     */
    private void updateChartView(){
        lineChart.dismiss();

        dataSet = new LineSet(chartLabels, chartValues);
        dataSet.setThickness(5f);

        if (isPositive) {
            dataSet.setColor(Colors.MAIN)
                    .setGradientFill(Colors.GRADIENT_HIGHLIGHT, null)
                    .setSmooth(true);
        }
        else {
            dataSet.setColor(Colors.RED)
                    .setGradientFill(Colors.GRADIENT_RED, null)
                    .setSmooth(true);
        }

        // find maximal value in chart
        float max = 0;
        for (int i = 0; i < chartValues.length; i++){
            if (chartValues[i] > max) max = chartValues[i];
        }

        // set maximal displayed value on axisY
        chartMaxValue = (int) max + 1;
        chartMaxValue = chartMaxValue + (CHART_VALUE_STEP - chartMaxValue %CHART_VALUE_STEP);

        // displays new data in the chart view
        lineChart.setAxisBorderValues(0, chartMaxValue,(chartMaxValue)/CHART_VALUE_STEP);
        lineChart.removeAllViews();
        lineChart.addData(dataSet);
        lineChart.show();
    }

    /**
     * Adds the information dot to a value in the chart
     * @param rect - the clicked area of the chart value
     */
    private void drawDataPoint(Rect rect){
        chartDot.setText(String.valueOf((int) chartValues[selectedChartColumn]));
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        int l = rect.centerX() - chartDot.getWidth()/2;
        int t = rect.centerY() - chartDot.getHeight()/2;
        param.setMargins(l, t, 0, 0);
        chartDot.setLayoutParams(param);
        if(isPositive)
            Utils.setBackgroundOfView(this, chartDot, R.drawable.chart_dot_good);
        else
            Utils.setBackgroundOfView(this, chartDot, R.drawable.chart_dot_bad);
        chartDot.setVisibility(View.VISIBLE);
    }

    /**
     * Removes the information bubble of the chart data value
     */
    private void removeDataPoint(){
        selectedChartColumn = 0;
        chartDot.setVisibility(View.GONE);
    }

    /**
     * Handles click on a chart data
     * @param columnNumber
     * @param r
     */
    private void chartDataClicked(int columnNumber, Rect r){
        if (columnNumber == 0 || columnNumber == lastInactiveColumn){
            updateChartData();
        }
        else {
            selectedChartColumn = columnNumber;
            updateChartView();
            drawDataPoint(r);
        }
    }

    @Override
    public void onClick(int i, int i1, Rect rect) {
        chartDataClicked(i1, rect);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.stat_week:
                if (range != RANGE_WEEK) setRangeTo(RANGE_WEEK);
                break;
            case R.id.stat_month:
                if (range != RANGE_MONTH) setRangeTo(RANGE_MONTH);
                break;
            case R.id.stat_year:
                if (range != RANGE_YEAR) setRangeTo(RANGE_YEAR);
                break;
            case R.id.btn_positive:
                if (!isPositive) changeColorScheme();
                break;
            case R.id.btn_negative:
                if (isPositive) changeColorScheme();
                break;
            case R.id.line_chart:
                if (selectedChartColumn > 0){
                    removeDataPoint();
                    updateChartView();
                }
                break;
        }
    }

    /**
     * Changes view based on the set range
     * @param setTo
     */
    private void setRangeTo(int setTo){

        switch (setTo){
            case RANGE_WEEK:
                if (isPositive) {
                    Utils.setBackgroundOfView(this, rangeWeek, R.drawable.border_bottom_selected_good);
                    rangeWeek.setTextColor(Colors.MAIN);
                }
                else {
                    Utils.setBackgroundOfView(this, rangeWeek, R.drawable.border_bottom_selected_bad);
                    rangeWeek.setTextColor(Colors.RED);
                }
                break;
            case RANGE_MONTH:
                if (isPositive){
                    Utils.setBackgroundOfView(this, rangeMonth, R.drawable.border_bottom_selected_good);
                    rangeMonth.setTextColor(Colors.MAIN);
                }
                else {
                    Utils.setBackgroundOfView(this, rangeMonth, R.drawable.border_bottom_selected_bad);
                    rangeMonth.setTextColor(Colors.RED);
                }
                break;
            case RANGE_YEAR:
                if (isPositive){
                    Utils.setBackgroundOfView(this, rangeYear, R.drawable.border_bottom_selected_good);
                    rangeYear.setTextColor(Colors.MAIN);
                }
                else {
                    Utils.setBackgroundOfView(this, rangeYear, R.drawable.border_bottom_selected_bad);
                    rangeYear.setTextColor(Colors.RED);
                }
                break;
        }
        switch (range){
            case RANGE_WEEK:
                Utils.setBackgroundOfView(this, rangeWeek, R.drawable.border_bottom);
                rangeWeek.setTextColor(Colors.GRAY);
                break;
            case RANGE_MONTH:
                Utils.setBackgroundOfView(this, rangeMonth, R.drawable.border_bottom);
                rangeMonth.setTextColor(Colors.GRAY);
                break;
            case RANGE_YEAR:
                Utils.setBackgroundOfView(this, rangeYear, R.drawable.border_bottom);
                rangeYear.setTextColor(Colors.GRAY);
                break;
        }

        range = setTo;

        updateChartData();
    }

    /**
     * changes color scheme to the opposite
     */
    private void changeColorScheme(){
        isPositive = !isPositive;

        int c = switchNeg.getCurrentTextColor();
        switchNeg.setTextColor(switchPos.getCurrentTextColor());
        switchPos.setTextColor(c);

        if (isPositive){ // positive
            Utils.setBackgroundOfView(this, switchPos, R.drawable.round_highlight_box_left);
            Utils.setBackgroundOfView(this, switchNeg, R.drawable.round_transparent);

            switch (range){
                case RANGE_WEEK:
                    Utils.setBackgroundOfView(this, rangeWeek, R.drawable.border_bottom_selected_good);
                    rangeWeek.setTextColor(Colors.MAIN);
                    break;
                case RANGE_MONTH:
                    Utils.setBackgroundOfView(this, rangeMonth, R.drawable.border_bottom_selected_good);
                    rangeMonth.setTextColor(Colors.MAIN);
                    break;
                case RANGE_YEAR:
                    Utils.setBackgroundOfView(this, rangeYear, R.drawable.border_bottom_selected_good);
                    rangeYear.setTextColor(Colors.MAIN);
                    break;
            }
        }
        else { // negative
            Utils.setBackgroundOfView(this, switchPos, R.drawable.round_transparent);
            Utils.setBackgroundOfView(this, switchNeg, R.drawable.round_bad_box_right);

            switch (range){
                case RANGE_WEEK:
                    Utils.setBackgroundOfView(this, rangeWeek, R.drawable.border_bottom_selected_bad);
                    rangeWeek.setTextColor(Colors.RED);
                    break;
                case RANGE_MONTH:
                    Utils.setBackgroundOfView(this, rangeMonth, R.drawable.border_bottom_selected_bad);
                    rangeMonth.setTextColor(Colors.RED);
                    break;
                case RANGE_YEAR:
                    Utils.setBackgroundOfView(this, rangeYear, R.drawable.border_bottom_selected_bad);
                    rangeYear.setTextColor(Colors.RED);
                    break;
            }
        }

        updateChartData();
    }

    @Override
    public void onBackPressed() {
        menu.goToActivity(menu.RECORDER_ID, RecorderActivity.class);
    }

}
