package sk.codekitchen.smartfuel.ui;

import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;

import sk.codekitchen.smartfuel.ui.GUI.*;
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

    private boolean posOrNeg = true; // true - positive | false - negative
    private LightTextView switchPos;
    private LightTextView switchNeg;

    private SemiboldTextView infoDistance;
    private SemiboldTextView infoPoints;
    private SemiboldTextView infoSuccess;

    private final static int CHART_VALUE_STEP = 5;
    private LineChartView lineChart;
    private LineSet dataSet;
    private int selectedChartColumn = 0;
    private int lastInactiveColumn = 8;

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

        // chart
        Paint linePaint = new Paint();
        linePaint.setColor(Colors.GRAY);
        linePaint.setAlpha(255);

        lineChart = (LineChartView) findViewById(R.id.linechart);

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

    }

    private void addDataToChart(String[] labels, float[] values){
        lineChart.dismiss();

        dataSet = new LineSet(labels, values);
        dataSet.setThickness(5f);
        if (posOrNeg) {
            dataSet.setColor(Colors.HIGHIGHT)
                    .setGradientFill(Colors.GRADIENT_HIGHLIGHT, null)
                    .setSmooth(true);
        }
        else {
            dataSet.setColor(Colors.RED)
                    .setGradientFill(Colors.GRADIENT_RED, null)
                    .setSmooth(true);
        }

        float max = 0;
        for (int i = 0; i < values.length; i++){
            if (values[i] > max) max = values[i];
        }

        int m = (int) max + 1;

        m = m + (CHART_VALUE_STEP - m%CHART_VALUE_STEP);

        lineChart.setAxisBorderValues(0, m,(m)/CHART_VALUE_STEP);
        lineChart.removeAllViews();
        lineChart.addData(dataSet);
        lineChart.show();
    }

    private void updateGraphData(){
        removeChartPoint();

        String[] labels = {"", "PO", "UT", "ST", "ST", "PI", "SO", "NE", ""};
        float[] values = {2f, 1f, 4f, 10f, 6f, 5f, 3f, 7f, 8f};

        switch (range){
            case RANGE_WEEK:

                break;
            case RANGE_MONTH:

                break;
            case RANGE_YEAR:

                break;

        }
        // show data in the chart
        addDataToChart(labels, values);
    }

    @Override
    public void onClick(int i, int i1, Rect rect) {
        addChartPoint(i1);
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
                if (!posOrNeg) setPosOrNeg();
                break;
            case R.id.btn_negative:
                if (posOrNeg) setPosOrNeg();
                break;
            case R.id.linechart:
                if (selectedChartColumn > 0) removeChartPoint();
                break;
        }
    }

    private void addChartPoint(int columnNumber){
        Toast.makeText(this, "Column " + columnNumber, Toast.LENGTH_LONG).show();
        selectedChartColumn = columnNumber;

        if (columnNumber == 0 || columnNumber == lastInactiveColumn){
            removeChartPoint();
        }
        else {
            dataSet.setDotsDrawable(Utils.getDrawable(this, R.drawable.chart_data));

        }
    }

    private void removeChartPoint(){
        selectedChartColumn = 0;
    }

    private void setRangeTo(int setTo){
        switch (setTo){
            case RANGE_WEEK:
                if (posOrNeg) {
                    Utils.setBackgroundOfView(this, rangeWeek, R.drawable.border_bottom_selected_good);
                    rangeWeek.setTextColor(Colors.HIGHIGHT);
                }
                else {
                    Utils.setBackgroundOfView(this, rangeWeek, R.drawable.border_bottom_selected_bad);
                    rangeWeek.setTextColor(Colors.RED);
                }
                break;
            case RANGE_MONTH:
                if (posOrNeg){
                    Utils.setBackgroundOfView(this, rangeMonth, R.drawable.border_bottom_selected_good);
                    rangeMonth.setTextColor(Colors.HIGHIGHT);
                }
                else {
                    Utils.setBackgroundOfView(this, rangeMonth, R.drawable.border_bottom_selected_bad);
                    rangeMonth.setTextColor(Colors.RED);
                }
                break;
            case RANGE_YEAR:
                if (posOrNeg){
                    Utils.setBackgroundOfView(this, rangeYear, R.drawable.border_bottom_selected_good);
                    rangeYear.setTextColor(Colors.HIGHIGHT);
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
        posOrNeg = !posOrNeg;

        int c = switchNeg.getCurrentTextColor();
        switchNeg.setTextColor(switchPos.getCurrentTextColor());
        switchPos.setTextColor(c);

        if (posOrNeg){ // positive
            Utils.setBackgroundOfView(this, switchPos, R.drawable.round_highlight_box_left);
            Utils.setBackgroundOfView(this, switchNeg, R.drawable.round_transparent);

            switch (range){
                case RANGE_WEEK:
                    Utils.setBackgroundOfView(this, rangeWeek, R.drawable.border_bottom_selected_good);
                    rangeWeek.setTextColor(Colors.HIGHIGHT);
                    break;
                case RANGE_MONTH:
                    Utils.setBackgroundOfView(this, rangeMonth, R.drawable.border_bottom_selected_good);
                    rangeMonth.setTextColor(Colors.HIGHIGHT);
                    break;
                case RANGE_YEAR:
                    Utils.setBackgroundOfView(this, rangeYear, R.drawable.border_bottom_selected_good);
                    rangeYear.setTextColor(Colors.HIGHIGHT);
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
