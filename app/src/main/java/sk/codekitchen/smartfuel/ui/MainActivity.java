package sk.codekitchen.smartfuel.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import sk.codekitchen.smartfuel.R;

public class MainActivity extends Activity {

    private boolean isLoggedIn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, RecorderActivity.class);
        startActivity(intent);

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

}

