package sk.codekitchen.smartfuel.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import sk.codekitchen.smartfuel.ui.GUI.*;
import sk.codekitchen.smartfuel.R;

public class ProfileActivity extends Activity implements View.OnClickListener{

    private MainMenu menu;

    private SemiboldTextView statKm;
    private SemiboldTextView statPoints;
    private SemiboldTextView statSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        menu = new MainMenu(this, MainMenu.USER_ID);
        setView();
    }

    private void setView(){
        statKm = (SemiboldTextView) findViewById(R.id.profile_km);
        statPoints = (SemiboldTextView) findViewById(R.id.profile_points);
        statSuccess = (SemiboldTextView) findViewById(R.id.profile_success);

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onBackPressed() {
        menu.goToActivity(menu.RECORDER_ID, RecorderActivity.class);
    }
}
