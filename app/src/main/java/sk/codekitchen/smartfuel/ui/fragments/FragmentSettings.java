package sk.codekitchen.smartfuel.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import sk.codekitchen.smartfuel.R;
import sk.codekitchen.smartfuel.ui.SmartFuelActivity;
import sk.codekitchen.smartfuel.ui.views.LightTextView;
import sk.codekitchen.smartfuel.ui.views.SemiboldTextView;
import sk.codekitchen.smartfuel.util.GLOBALS;

/**
 * @author Gabriel Lehocky
 */
public class FragmentSettings extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private Switch audio;
    private LightTextView units;
    private SemiboldTextView about;

    private boolean isAudio;
    private boolean isMph;

    private SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        audio = (Switch) view.findViewById(R.id.set_audio_switch);
        units = (LightTextView) view.findViewById(R.id.set_units_switch);
        about = (SemiboldTextView) view.findViewById(R.id.set_about_text);

        audio.setOnCheckedChangeListener(this);
        units.setOnClickListener(this);
        about.setOnClickListener(this);

        loadSavedSettings();

        return view;
    }

    private void loadSavedSettings(){
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        isMph = preferences.getBoolean(GLOBALS.SETTINGS_IS_MPH, false);
        isAudio = preferences.getBoolean(GLOBALS.SETTINGS_IS_AUDIO, true);

        audio.setChecked(isAudio);
        showActualUnit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.set_units_switch:
                isMph = !isMph;
                showActualUnit();
                preferences.edit().putBoolean(GLOBALS.SETTINGS_IS_MPH, isMph).apply();
                break;
            case R.id.set_about_text:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.set_audio_switch:
                isAudio = audio.isChecked();
                preferences.edit().putBoolean(GLOBALS.SETTINGS_IS_AUDIO, isAudio).apply();
                break;
        }
    }

    private void showActualUnit(){
        if (isMph) units.setText(getString(R.string.rec_mph));
        else units.setText(getString(R.string.rec_kmph));
    }

}