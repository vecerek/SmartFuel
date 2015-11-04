package sk.codekitchen.smartfuel.ui;

import android.app.Activity;
import android.os.Bundle;

import sk.codekitchen.smartfuel.R;

public class SettingsActivity extends Activity {

    private MainMenu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_settings);
        menu = new MainMenu(this, MainMenu.SETTINGS_ID);
    }

    @Override
    public void onBackPressed() {
        menu.goToActivity(menu.RECORDER_ID, RecorderActivity.class);
    }
}

