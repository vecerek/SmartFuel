package sk.codekitchen.smartfuel.ui.GUI;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author Gabriel Lehocky
 */
public class SemiboldTextView extends TextView {

    public SemiboldTextView(Context context, AttributeSet attrs){
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "Fonts/ProximaNova-Semibold.otf"));
    }
}