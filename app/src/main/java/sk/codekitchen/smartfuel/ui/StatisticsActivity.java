package sk.codekitchen.smartfuel.ui;

import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.view.AxisController;

import sk.codekitchen.smartfuel.ui.gui.*;
import sk.codekitchen.smartfuel.R;

/**
 * @author Gabriel Lehocky
 */
public class StatisticsActivity extends Activity implements View.OnClickListener, OnEntryClickListener {

    // Line Chart Usage Demo:
    // https://github.com/diogobernardino/WilliamChart/blob/master/sample/src/com/db/williamchartdemo/LineFragment.java
    // https://github.com/diogobernardino/WilliamChart/wiki/%282%29-Chart
    // https://github.com/diogobernardino/WilliamChart/wiki/%283%29-Line-Chart

    private MainMenu menu;

    private final static int RANGE_WEEK = 1;
    private final static int RANGE_MONTH = 2;
    private final static int RANGE_YEAR = 3;
    private int range = RANGE_WEEK;
    private LightTextView rangeWeek;
    private LightTextView rangeMonth;
    private LightTextView rangeYear;

    private boolean isPositive = true; // true - positive | false - negative
    private LightTextView switchPos;
    private LightTextView switchNeg;

    private SemiboldTextView infoDistance;
    private SemiboldTextView infoPoints;
    private SemiboldTextView infoSuccess;

    private final static int CHART_VALUE_STEP = 5;
    private CustomLineChartView lineChart;
    private CustomLineSet dataSet;
    private LightTextView chartDot;
    private int selectedChartColumn = 0;
    private int lastInactiveColumn = 8;
    private int chartMaxValue;

    private String[] chartLabels;
    private float[] chartValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        menu = new MainMenu(this, MainMenu.STATISTICS_ID);
        setView();

        updateGraphData();
    }

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

        lineChart = (CustomLineChartView) findViewById(R.id.line_chart);
        lineChart.setOnClickListener(this);
        lineChart.setOnEntryClickListener(this);
        lineChart.setTopSpacing(Tools.fromDpToPx(15))
                .setXLabels(AxisController.LabelPosition.OUTSIDE)
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

    private void addDataToChart(){
        lineChart.dismiss();

        dataSet = new CustomLineSet(chartLabels, chartValues);
        dataSet.setThickness(5f);

        if (isPositive) {
            if (selectedChartColumn != 0)
                dataSet.setOneDot(selectedChartColumn, Colors.WHITE, 3f, Colors.MAIN, 10f);
            dataSet.setColor(Colors.MAIN)
                    .setGradientFill(Colors.GRADIENT_HIGHLIGHT, null)
                    .setSmooth(true);
        }
        else {
            if (selectedChartColumn != 0)
                dataSet.setOneDot(selectedChartColumn, Colors.WHITE, 3f, Colors.RED, 10f);
            dataSet.setColor(Colors.RED)
                    .setGradientFill(Colors.GRADIENT_RED, null)
                    .setSmooth(true);
        }

        float max = 0;
        for (int i = 0; i < chartValues.length; i++){
            if (chartValues[i] > max) max = chartValues[i];
        }

        chartMaxValue = (int) max + 1;

        chartMaxValue = chartMaxValue + (CHART_VALUE_STEP - chartMaxValue %CHART_VALUE_STEP);

        lineChart.setAxisBorderValues(0, chartMaxValue,(chartMaxValue)/CHART_VALUE_STEP);
        lineChart.removeAllViews();
        lineChart.addData(dataSet);
        lineChart.show();
    }

    private void drawDataPoint(Rect rect){
        chartDot.setText(String.valueOf(chartValues[selectedChartColumn]));
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        int l = rect.centerX() - chartDot.getWidth()/2;
        int r = rect.centerX() + chartDot.getWidth()/2;
        param.setMargins(l,0,r,0);
        chartDot.setLayoutParams(param);
        chartDot.setVisibility(View.VISIBLE);
    }

    private void clearDataPoint(){
        chartDot.setVisibility(View.GONE);
    }

    private void updateGraphData(){
        removeChartPoint();

        String[] lab = {"", "PO", "UT", "ST", "ST", "PI", "SO", "NE", ""};
        float[] val = {2f, 1f, 4f, 10f, 6f, 5f, 3f, 7f, 8f};

        chartLabels = lab;
        chartValues = val;

        switch (range){
            case RANGE_WEEK:

                break;
            case RANGE_MONTH:

                break;
            case RANGE_YEAR:

                break;

        }
        // show data in the chart
        addDataToChart();
    }

    @Override
    public void onClick(int i, int i1, Rect rect) {
        addChartPoint(i1, rect);
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
                if (!isPositive) setPosOrNeg();
                break;
            case R.id.btn_negative:
                if (isPositive) setPosOrNeg();
                break;
            case R.id.line_chart:
                if (selectedChartColumn > 0) {
                    removeChartPoint();
                    addDataToChart();
                }
                break;
        }
    }

    private void addChartPoint(int columnNumber, Rect r){
        if (columnNumber == 0 || columnNumber == lastInactiveColumn){
            removeChartPoint();
            clearDataPoint();
            addDataToChart();
        }
        else {
            selectedChartColumn = columnNumber;
            addDataToChart();
            drawDataPoint(r);
        }
    }

    private void removeChartPoint(){
        selectedChartColumn = 0;
    }

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

        updateGraphData();
    }

    private void setPosOrNeg(){
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
        updateGraphData();
    }

    @Override
    public void onBackPressed() {
        menu.goToActivity(menu.RECORDER_ID, RecorderActivity.class);
    }

}
