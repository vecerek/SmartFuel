package sk.codekitchen.smartfuel.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.Switch;

import sk.codekitchen.smartfuel.R;
import sk.codekitchen.smartfuel.ui.views.LightTextView;
import sk.codekitchen.smartfuel.ui.views.SemiboldTextView;

/**
 * @author Gabriel Lehocky
 */
public class FragmentSettings extends Fragment {

    private Spinner language;
    private Switch audio;
    private LightTextView units;
    private SemiboldTextView about;

    boolean isAudio;
    boolean isMph;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        language = (Spinner) view.findViewById(R.id.set_language_spinner);
        audio = (Switch) view.findViewById(R.id.set_audio_switch);
        units = (LightTextView) view.findViewById(R.id.set_units_switch);
        about = (SemiboldTextView) view.findViewById(R.id.set_about_text);

        loadSavedSetings();

        /**
         * TODO: Click listener for units and about, set spinner and save new set values to shared prefs.
         */

        return view;
    }

    private void loadSavedSetings(){

        /**
         * TODO : Load from shared preferences
         */

    }
}