package sk.codekitchen.smartfuel.ui.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author Gabriel Lehocky
 *
 * Extends TextView by custom Light font type
 */
public class LightTextView extends TextView {

    public LightTextView(Context context) {
        super (context);
        init(context);
    }

    public LightTextView(Context context, AttributeSet attrs){
        super(context, attrs);
        init(context);
    }

    public LightTextView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context){
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "Fonts/ProximaNova-Light.otf"));
    }
}