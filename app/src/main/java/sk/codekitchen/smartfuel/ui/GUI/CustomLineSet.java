package sk.codekitchen.smartfuel.ui.GUI;

import com.db.chart.model.ChartEntry;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;

/**
 * Created by L on 15/10/12.
 */
public class CustomLineSet extends LineSet {

    public CustomLineSet(String[] labels, float[] values){
        super(labels, values);
    }

    public LineSet setOneDot(int id, int color, float strokeSize, int strokeColor, float radius){

        ChartEntry e = getEntries().get(id);

        e.setColor(color);
        ((Point) e).setStrokeColor(strokeColor);
        ((Point) e).setStrokeThickness(strokeSize);
        ((Point) e).setRadius(radius);

        return this;
    }

}
