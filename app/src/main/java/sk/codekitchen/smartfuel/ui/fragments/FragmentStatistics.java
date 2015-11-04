package sk.codekitchen.smartfuel.ui.fragments;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;

import sk.codekitchen.smartfuel.R;
import sk.codekitchen.smartfuel.ui.views.Colors;
import sk.codekitchen.smartfuel.ui.views.LightTextView;
import sk.codekitchen.smartfuel.ui.views.SemiboldTextView;

/**
 * @author Gabriel Lehocky
 */
public class FragmentStatistics extends Fragment implements View.OnClickListener, OnEntryClickListener {

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

    // line chart
    private LineChartView lineChart;
    private LineSet dataSet;
    private LightTextView chartDot;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        // range settings
        rangeWeek = (LightTextView) view.findViewById(R.id.stat_week);
        rangeWeek.setOnClickListener(this);
        rangeMonth = (LightTextView) view.findViewById(R.id.stat_month);
        rangeMonth.setOnClickListener(this);
        rangeYear = (LightTextView) view.findViewById(R.id.stat_year);
        rangeYear.setOnClickListener(this);

        // positive / negative
        switchPos = (LightTextView) view.findViewById(R.id.btn_positive);
        switchPos.setOnClickListener(this);
        switchNeg = (LightTextView) view.findViewById(R.id.btn_negative);
        switchNeg.setOnClickListener(this);

        // information
        infoDistance = (SemiboldTextView) view.findViewById(R.id.stat_km);
        infoPoints = (SemiboldTextView) view.findViewById(R.id.stat_points);
        infoSuccess = (SemiboldTextView) view.findViewById(R.id.stat_success);

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

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onClick(int i, int i1, Rect rect) {

    }
}