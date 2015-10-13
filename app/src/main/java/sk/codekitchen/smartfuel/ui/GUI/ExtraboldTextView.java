package sk.codekitchen.smartfuel.ui.GUI;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author Gabriel Lehocky
 *
 * Extends TextView by custom Extrabold font type
 */
public class ExtraboldTextView extends TextView {

    public ExtraboldTextView(Context context, AttributeSet attrs){
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "Fonts/ProximaNova-Extrabold.otf"));
    }
}
