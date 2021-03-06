package sk.codekitchen.smartfuel.ui.fragments;

import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;

import java.util.ArrayList;
import java.util.HashMap;

import sk.codekitchen.smartfuel.R;
import sk.codekitchen.smartfuel.model.Statistics;
import sk.codekitchen.smartfuel.ui.views.Colors;
import sk.codekitchen.smartfuel.ui.views.LightTextView;
import sk.codekitchen.smartfuel.ui.views.SemiboldTextView;
import sk.codekitchen.smartfuel.ui.views.Utils;
import sk.codekitchen.smartfuel.util.GLOBALS;
import sk.codekitchen.smartfuel.util.Units;

/**
 * @author Gabriel Lehocky
 */
public class FragmentStatistics extends Fragment implements View.OnClickListener, OnEntryClickListener {

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
    private LinearLayout switchStat;

    // information text views
    private SemiboldTextView infoDistance;
    private LightTextView infoDistanceUnit;
    private SemiboldTextView infoPoints;
    private SemiboldTextView infoSuccess;

    // line chart data
    private final static int CHART_VALUE_STEP = 5;
    private LineChartView lineChart;
    private LightTextView chartDot;

    private boolean isSelectedChartColumn = false;

    private SharedPreferences preferences;
    private boolean isMph;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        isMph = preferences.getBoolean(GLOBALS.SETTINGS_IS_MPH, false);
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        (new LoadStatsDataTask()).execute((Void) null);

        // range settings
        weekTab = (LightTextView) view.findViewById(R.id.stat_week);
        weekTab.setOnClickListener(this);
        monthTab = (LightTextView) view.findViewById(R.id.stat_month);
        monthTab.setOnClickListener(this);
        yearTab = (LightTextView) view.findViewById(R.id.stat_year);
        yearTab.setOnClickListener(this);

        // positive / negative
        switchPos = (LightTextView) view.findViewById(R.id.btn_positive);
        switchNeg = (LightTextView) view.findViewById(R.id.btn_negative);
        switchStat = (LinearLayout) view.findViewById(R.id.stat_switch);
        switchStat.setOnClickListener(this);

        // information
        infoDistance = (SemiboldTextView) view.findViewById(R.id.stat_distance);
        infoPoints = (SemiboldTextView) view.findViewById(R.id.stat_points);
        infoSuccess = (SemiboldTextView) view.findViewById(R.id.stat_success);
        infoDistanceUnit = (LightTextView) view.findViewById(R.id.stat_distance_unit);

        //chart
        Paint linePaint = new Paint();
        linePaint.setColor(Colors.GRAY);
        linePaint.setAlpha(255);

        lineChart = (LineChartView) view.findViewById(R.id.line_chart);
        lineChart.setOnClickListener(this);
        lineChart.setOnEntryClickListener(this);
        lineChart.setXLabels(AxisController.LabelPosition.OUTSIDE)
                .setYLabels(AxisController.LabelPosition.INSIDE)
                .setLabelsColor(Colors.GRAY)
                .setFontSize(32)
                .setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Fonts/ProximaNova-Light.otf"))
                .setXAxis(false)
                .setYAxis(false)
                .setAxisThickness(1f)
                .setBorderSpacing(0f)
                .setTopSpacing(10f)
                .setAxisColor(Colors.GRAY);

        chartDot = (LightTextView) view.findViewById(R.id.chart_dot);

        return view;
    }

    public void loadUnits() {
        if (isMph) {
            infoDistanceUnit.setText(getString(R.string.profile_total_distance_mile));
        }
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
            case R.id.stat_switch:
                current.changeColorScheme();
                break;
            case R.id.line_chart:
                if (isSelectedChartColumn){
                    removeDataPoint();
                    current.updateChart();
                }
                break;
        }
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

            distance = (int) Units.getPreferredDistance(data.distance, isMph);
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
                valuesPos[i] = Units.getPreferredDistance(col.correctDistance, isMph);
                valuesNeg[i] = Units.getPreferredDistance(col.speedingDistance, isMph);
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
                    Utils.setBackgroundOfView(getActivity(), switchNeg, R.drawable.round_bad_box_right);
                    Utils.setBackgroundOfView(getActivity(), switchPos, R.drawable.round_transparent);
                    Utils.setBackgroundOfView(getActivity(), ltv, R.drawable.border_bottom_selected_bad);
                    ltv.setTextColor(Colors.RED);
                } else { // Turn statistics to positive view
                    Utils.setBackgroundOfView(getActivity(), switchNeg, R.drawable.round_transparent);
                    Utils.setBackgroundOfView(getActivity(), switchPos, R.drawable.round_highlight_box_left);
                    Utils.setBackgroundOfView(getActivity(), ltv, R.drawable.border_bottom_selected_good);
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
                Utils.setBackgroundOfView(getActivity(), chartDot, R.drawable.chart_dot_good);
            } else {
                Utils.setBackgroundOfView(getActivity(), chartDot, R.drawable.chart_dot_bad);
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
                    Utils.setBackgroundOfView(getActivity(), ltv, R.drawable.border_bottom_selected_good);
                    ltv.setTextColor(Colors.MAIN);
                } else {
                    Utils.setBackgroundOfView(getActivity(), ltv, R.drawable.border_bottom_selected_bad);
                    ltv.setTextColor(Colors.RED);
                }
            }
            updateChart();
        }

        protected void deactivate() {
            if (ltv != null) {
                Utils.setBackgroundOfView(getActivity(), ltv, R.drawable.border_bottom);
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
                        .setSmooth(false);
            } else {
                dataSet.setColor(Colors.RED)
                        .setGradientFill(Colors.GRADIENT_RED, null)
                        .setSmooth(false);
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
                stats = Statistics.getInstance(getActivity().getApplicationContext());
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