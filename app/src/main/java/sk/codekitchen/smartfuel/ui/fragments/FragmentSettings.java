package sk.codekitchen.smartfuel.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class FragmentSettings extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemClickListener {

    private static final ArrayList<Language> langs;
    static{
        langs = new ArrayList<>();
        langs.add(new Language("English", "en"));
        langs.add(new Language("Slovenčina", "sk"));
    }

    private LightTextView language;
    private Switch audio;
    private LightTextView units;
    private SemiboldTextView about;

    private boolean isAudio;
    private boolean isMph;
    private Locale myLocale;
    private String actualLocale;

    private SharedPreferences preferences;
    private Dialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        language = (LightTextView) view.findViewById(R.id.set_language);
        audio = (Switch) view.findViewById(R.id.set_audio_switch);
        units = (LightTextView) view.findViewById(R.id.set_units_switch);
        about = (SemiboldTextView) view.findViewById(R.id.set_about_text);

        language.setOnClickListener(this);
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
        actualLocale = preferences.getString(GLOBALS.SETTINGS_LANG, "");

        String langName = getString(R.string.set_automatic);
        for (int i = 0; i < langs.size(); i++)
            if (langs.get(i).code.toString().equals(actualLocale.toString())){
                langName = langs.get(i).name.toString();
                break;
            }
        language.setText(langName);
        audio.setChecked(isAudio);
        showActualUnit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.set_units_switch:
                isMph = !isMph;
                showActualUnit();
                preferences.edit().putBoolean(GLOBALS.SETTINGS_IS_MPH, isMph).commit();
                break;
            case R.id.set_about_text:
                break;
            case R.id.set_language:
                changeLanguageDialog();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.set_audio_switch:
                isAudio = audio.isChecked();
                preferences.edit().putBoolean(GLOBALS.SETTINGS_IS_AUDIO, isAudio).commit();
                break;
        }
    }

    private void changeLanguageDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        ListView langList = new ListView(getContext());
        langList.setOnItemClickListener(this);
        langList.setAdapter(new LanguageAdapter(getContext(), R.layout.language_spinner_item, langs));
        builder.setView(langList);
        dialog = builder.create();
        dialog.show();
    }

    private void showActualUnit(){
        if (isMph) units.setText(getString(R.string.rec_mph));
        else units.setText(getString(R.string.rec_kmph));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dialog.dismiss();
        String lang = langs.get(position).code;
        preferences.edit().putString(GLOBALS.SETTINGS_LANG, lang).commit();
        SmartFuelActivity a = (SmartFuelActivity) getActivity();
        a.setLocale(lang);
    }

    private static class Language{
        public String name;
        public String code;

        public Language(String n, String c){
            name = n;
            code = c;
        }
    }

    private class LanguageAdapter extends ArrayAdapter<Language>{

        private Context context;
        private ArrayList<Language> values;
        int layout;

        public LanguageAdapter(Context c, int viewLayout, ArrayList<Language> array){
            super(c, viewLayout, array);
            context = c;
            values = array;
            layout = viewLayout;
        }
        public int getCount(){
            return values.size();
        }

        public Language getItem(int position){
            return values.get(position);
        }

        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = LayoutInflater.from(context);
            View langItem = inflater.inflate(layout, null);

            LightTextView label = (LightTextView) langItem.findViewById(R.id.dialog_language_name);
            label.setText(values.get(position).name.toString());

            return langItem;
        }
    }

}