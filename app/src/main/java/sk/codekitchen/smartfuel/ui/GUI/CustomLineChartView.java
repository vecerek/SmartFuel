package sk.codekitchen.smartfuel.ui.gui;

import android.content.Context;
import android.graphics.Region;
import android.util.AttributeSet;

import com.db.chart.model.ChartEntry;
import com.db.chart.model.ChartSet;
import com.db.chart.view.LineChartView;

import java.util.ArrayList;

/**
 * Created by L on 15/10/12.
 */
public class CustomLineChartView extends LineChartView {

    public CustomLineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomLineChartView(Context context) {
        super(context);
    }

    @Override
    public ArrayList<ArrayList<Region>> defineRegions(ArrayList<ChartSet> data){

        ArrayList<ArrayList<Region>> result = new ArrayList<>(data.size());

        ArrayList<Region> regionSet;
        float x;
        float y;
        for(ChartSet set : data){

            float radius = getResources().getDimension(com.db.williamchart.R.dimen.dot_region_radius);
            if (set.size() > 2)
                radius = getWidth()/(2*set.size() - 2);

            regionSet = new ArrayList<>(set.size());
            for(ChartEntry e : set.getEntries()){


                x = e.getX();
                y = e.getY();
                regionSet.add(new Region((int)(x - radius),
                        (int)(y - radius),
                        (int)(x + radius),
                        (int)(y + radius)));
            }
            result.add(regionSet);
        }

        return result;

    }

}
