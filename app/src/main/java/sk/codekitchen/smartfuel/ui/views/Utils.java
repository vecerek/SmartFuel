package sk.codekitchen.smartfuel.ui.views;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ProgressBar;

/**
 * @author Gabriel Lehocky
 *
 * Implements static methods that are used to distinguish between different API levels
 */
public class Utils {

    /**
     * sets the background of a view to a resource drawable
     * @param a
     * @param v
     * @param d - must be from R.drawable
     */
    public static void setBackgroundOfView(Activity a, View v, int d){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
            // FROM API 21
            v.setBackground(a.getDrawable(d));
        }
        else if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN){
            // UNDER API 16
            v.setBackgroundDrawable(a.getResources().getDrawable(d));
        }
        else {
            // FROM API 16 TO 20
            v.setBackground(a.getResources().getDrawable(d));
        }
    }

    /**
     * Returns Drawable based on the id
     * @param a
     * @param id - must be from R.drawable
     * @return
     */
    public static Drawable getDrawable(Activity a, int id){
        Drawable d = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
            // FROM API 21
            d = a.getDrawable(id);
        }
        else {
            // FROM API 16 TO 20
            d = a.getResources().getDrawable(id);
        }

        return d;
    }

    /**
     * Sets the style of a progressbar to the style of the drawable resource
     * @param a
     * @param p
     * @param d - must be from R.drawable
     */
    public static void setProgressBarProgress(Activity a, ProgressBar p, int d){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
            // FROM API 21
            p.setProgressDrawable(a.getDrawable(d));
        }
        else{
            // UNDER API 21
            p.setProgressDrawable(a.getResources().getDrawable(d));
        }
    }
}