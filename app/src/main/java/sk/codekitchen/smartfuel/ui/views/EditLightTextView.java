package sk.codekitchen.smartfuel.ui.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * @author Gabriel Lehocky
 *
 * Extends EditText view by custom font type
 */
public class EditLightTextView extends EditText{

    public EditLightTextView(Context context) {
        super (context);
        init(context);
    }

    public EditLightTextView(Context context, AttributeSet attrs){
        super(context, attrs);
        init(context);
    }

    public EditLightTextView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context){
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "Fonts/ProximaNova-Light.otf"));
    }
}
