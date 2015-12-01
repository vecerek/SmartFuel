package sk.codekitchen.smartfuel.ui;

import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;

import java.util.ArrayList;
import java.util.HashMap;

import sk.codekitchen.smartfuel.model.Statistics;
import sk.codekitchen.smartfuel.ui.views.*;
import sk.codekitchen.smartfuel.R;

/**
 * @author Gabriel Lehocky
 *
 * Activity that shows the users statistic data
 */
public class StatisticsActivity extends Activity implements View.OnClickListener, OnEntryClickListener {

    private MainMenu menu;

    //tabs
    private StatsTab current;
    private StatsTab week;
    private StatsTab month;
    private StatsTab year;

	private LightTextView weekTab;
	private LightTextView monthTab;
	private LightTextView yearTab;

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
    private LightTextView chartDot;

    private boolean isSelectedChartColumn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_statistics);

        menu = new MainMenu(this, MainMenu.STATISTICS_ID);
	    setView();
	    (new LoadStatsDataTask()).execute((Void) null);
    }

    /**
     * Creates the default view of the activity and fills default values
     */
    private void setView(){
        // range settings
        weekTab = (LightTextView) findViewById(R.id.stat_week);
        weekTab.setOnClickListener(this);
        monthTab = (LightTextView) findViewById(R.id.stat_month);
        monthTab.setOnClickListener(this);
        yearTab = (LightTextView) findViewById(R.id.stat_year);
        yearTab.setOnClickListener(this);

        // positive / negative
        switchPos = (LightTextView) findViewById(R.id.btn_positive);
        switchPos.setOnClickListener(this);
        switchNeg = (LightTextView) findViewById(R.id.btn_negative);
        switchNeg.setOnClickListener(this);

        // information
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
     * Removes the information bubble of the chart data value
     */
    private void removeDataPoint() {
        isSelectedChartColumn = false;
        chartDot.setVisibility(View.GONE);
    }

    /**
     * Handles click on a chart data
     * @param columnNumber
     * @param r
     */
    private void chartDataClicked(int columnNumber, Rect r) {
        if (current.isBlankColumn(columnNumber)) {
            removeDataPoint();
        } else {
            isSelectedChartColumn = true;
            current.drawDataPoint(r, columnNumber);
        }
        current.updateChart();
    }

    @Override
    public void onClick(int i, int i1, Rect rect) {
        chartDataClicked(i1, rect);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stat_week:
                if (current != week) {
                    current.deactivate();
                    current = week;
                    current.activate();
                }
                break;
            case R.id.stat_month:
                if (current != month) {
                    current.deactivate();
                    current = month;
                    current.activate();
                }
                break;
            case R.id.stat_year:
                if (current != year) {
                    current.deactivate();
                    current = year;
                    current.activate();
                }
                break;
            case R.id.btn_positive:
                if (!isPositive) current.changeColorScheme();
                break;
            case R.id.btn_negative:
                if (isPositive) current.changeColorScheme();
                break;
            case R.id.line_chart:
                if (isSelectedChartColumn){
                    removeDataPoint();
                    current.updateChart();
                }
                break;
        }
    }



    @Override
    public void onBackPressed() {
        menu.goToActivity(MainMenu.RECORDER_ID, RecorderActivity.class);
    }

    class StatsTab {

        public LightTextView ltv;

	    private String[] chartLabels;
	    private HashMap<Boolean, float[]> chartValues = new HashMap<>();
	    private HashMap<Boolean, Integer> chartMaxValues = new HashMap<>();
	    private HashMap<Boolean, LineSet> lineSets = new HashMap<>();

        private int distance;
        private int points;
        private int successRate;

        StatsTab(LightTextView ltv, Statistics.TabData data) {
            this.ltv = ltv;

	        distance = data.distance;
	        points = data.points;
	        successRate = data.successRate;
			processStatisticData(data.cols);
        }

	    protected void processStatisticData(ArrayList<Statistics.TabData.ColumnData> cols) {
		    int size = cols.size() + 2;
		    chartLabels = new String[size];
		    float[] valuesPos = new float[size];
		    float[] valuesNeg = new float[size];

		    chartLabels[0] = "";
		    valuesPos[0] = 0f;
		    valuesNeg[0] = 0f;

		    int maxPos, maxNeg;
		    maxPos = maxNeg = 0;
		    int i = 1;
		    for (Statistics.TabData.ColumnData col : cols) {
			    chartLabels[i] = col.key;
			    valuesPos[i] = (float) col.correctDistance;
			    valuesNeg[i] = (float) col.speedingDistance;
			    i++;
			    if (col.correctDistance > maxPos) maxPos = col.correctDistance;
			    if (col.speedingDistance > maxNeg) maxNeg = col.speedingDistance;
			}
		    chartLabels[i] = "";
		    valuesPos[i] = 0f;
		    valuesNeg[i] = 0f;

		    chartValues.put(true, valuesPos);
		    chartValues.put(false, valuesNeg);

		    lineSets.put(true, new LineSet(chartLabels, valuesPos));
		    lineSets.put(false, new LineSet(chartLabels, valuesNeg));

		    maxPos += 1 + (CHART_VALUE_STEP - maxPos %CHART_VALUE_STEP);
		    maxNeg += 1 + (CHART_VALUE_STEP - maxNeg %CHART_VALUE_STEP);

		    chartMaxValues.put(true, maxPos);
		    chartMaxValues.put(false, maxNeg);
	    }

        /**
         * changes color scheme to the opposite
         */
        protected void changeColorScheme() {
            int posColor = switchPos.getCurrentTextColor();
            int negColor = switchNeg.getCurrentTextColor();
            switchNeg.setTextColor(posColor);
            switchPos.setTextColor(negColor);

            if (ltv != null) {
                if (isPositive) { // Turn statistics to negative view
                    Utils.setBackgroundOfView(StatisticsActivity.this, switchNeg, R.drawable.round_bad_box_right);
                    Utils.setBackgroundOfView(StatisticsActivity.this, switchPos, R.drawable.round_transparent);
                    Utils.setBackgroundOfView(StatisticsActivity.this, ltv, R.drawable.border_bottom_selected_bad);
                    ltv.setTextColor(Colors.RED);
                } else { // Turn statistics to positive view
                    Utils.setBackgroundOfView(StatisticsActivity.this, switchNeg, R.drawable.round_transparent);
                    Utils.setBackgroundOfView(StatisticsActivity.this, switchPos, R.drawable.round_highlight_box_left);
                    Utils.setBackgroundOfView(StatisticsActivity.this, ltv, R.drawable.border_bottom_selected_good);
                    ltv.setTextColor(Colors.MAIN);
                }
            }

            isPositive = !isPositive;
            updateChart();
        }

        /**
         * Adds the information dot to a value in the chart
         * @param rect - the clicked area of the chart value
         */
        private void drawDataPoint(Rect rect, int col) {
            chartDot.setText(String.valueOf((int) chartValues.get(isPositive)[col]));
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            int l = rect.centerX() - chartDot.getWidth()/2;
            int t = rect.centerY() - chartDot.getHeight()/2;
            param.setMargins(l, t, 0, 0);
            chartDot.setLayoutParams(param);
            if(isPositive) {
                Utils.setBackgroundOfView(StatisticsActivity.this, chartDot, R.drawable.chart_dot_good);
            } else {
                Utils.setBackgroundOfView(StatisticsActivity.this, chartDot, R.drawable.chart_dot_bad);
            }
            chartDot.setVisibility(View.VISIBLE);
        }

        /**
         * Changes view based on the set range
         */
        protected void activate() {
	        infoDistance.setText(String.valueOf(this.distance));
	        infoPoints.setText(String.valueOf(this.points));
	        infoSuccess.setText(String.valueOf(this.successRate) + " %");

            if (ltv != null) {
                if (isPositive) {
                    Utils.setBackgroundOfView(StatisticsActivity.this, ltv, R.drawable.border_bottom_selected_good);
                    ltv.setTextColor(Colors.MAIN);
                } else {
                    Utils.setBackgroundOfView(StatisticsActivity.this, ltv, R.drawable.border_bottom_selected_bad);
                    ltv.setTextColor(Colors.RED);
                }
            }
            updateChart();
        }

        protected void deactivate() {
            if (ltv != null) {
                Utils.setBackgroundOfView(StatisticsActivity.this, ltv, R.drawable.border_bottom);
                ltv.setTextColor(Colors.GRAY);
            }
        }

        protected boolean isBlankColumn(int col) {
            return col == 0 || col == (chartLabels.length - 1);
        }

        /**
         * Fills chart view with the actual data
         */
        protected void updateChart() {
            removeDataPoint();
            lineChart.dismiss();

	        LineSet dataSet = lineSets.get(isPositive);
			dataSet.setThickness(5f);

            if (isPositive) {
                dataSet.setColor(Colors.MAIN)
                        .setGradientFill(Colors.GRADIENT_HIGHLIGHT, null)
                        .setSmooth(true);
            } else {
                dataSet.setColor(Colors.RED)
                        .setGradientFill(Colors.GRADIENT_RED, null)
                        .setSmooth(true);
            }

            int max = chartMaxValues.get(isPositive);
            int step = max/CHART_VALUE_STEP;
            max -= max % step;

            // displays new data in the chart view
            lineChart.setAxisBorderValues(0, max, step);
            lineChart.removeAllViews();
            lineChart.addData(dataSet);
            lineChart.show();
        }

    }

    private class LoadStatsDataTask extends AsyncTask<Void, Void, Void> {

        private Statistics stats;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                stats = Statistics.getInstance(getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

	    @Override
	    protected void onPostExecute(Void param) {
		    if (stats != null) {
				week = new StatsTab(weekTab, stats.week);
			    month = new StatsTab(monthTab, stats.month);
			    year = new StatsTab(yearTab, stats.year);
			    current = week;
			    current.activate();
		    }
	    }
    }

}
