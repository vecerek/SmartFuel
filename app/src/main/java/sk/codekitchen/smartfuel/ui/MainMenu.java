package sk.codekitchen.smartfuel.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.view.View.OnClickListener;
import android.widget.Toast;

import sk.codekitchen.smartfuel.R;

/**
 * @author Gabriel Lehocky
 */
public class MainMenu implements OnClickListener{

    public final static int RECORDER_ID = 1;
    public final static int STATISTICS_ID = 2;
    public final static int SHOP_ID = 3;
    public final static int USER_ID = 4;
    public final static int SETTINGS_ID = 5;

    public ImageButton recBtn, statBtn, shopBtn, usrBtn, setBtn;
    private Activity parent;
    private int actualId;

    public MainMenu(Activity p, int id){
        parent = p;
        actualId = id;

        recBtn =  (ImageButton) parent.findViewById(R.id.menu_record);
        statBtn =  (ImageButton) parent.findViewById(R.id.menu_stats);
        shopBtn =  (ImageButton) parent.findViewById(R.id.menu_shop);
        usrBtn =  (ImageButton) parent.findViewById(R.id.menu_user);
        setBtn =  (ImageButton) parent.findViewById(R.id.menu_settings);

        recBtn.setOnClickListener(this);
        statBtn.setOnClickListener(this);
        shopBtn.setOnClickListener(this);
        usrBtn.setOnClickListener(this);
        setBtn.setOnClickListener(this);

        switch (actualId) {
            case RECORDER_ID:
                recBtn.setAlpha(1f); break;
            case STATISTICS_ID:
                statBtn.setAlpha(1f); break;
            case SHOP_ID:
                shopBtn.setAlpha(1f); break;
            case USER_ID:
                usrBtn.setAlpha(1f); break;
            case SETTINGS_ID:
                setBtn.setAlpha(1f); break;
        }
    }

    /**
     *  TODO: !!!
     *      CHECK if activity is already running, so It wont start a new activity
     *      just go back to that one.
     */
    @Override
    public void onClick(View v){
        // CHANGE ACTIVITY HERE
        switch (v.getId()){
            case R.id.menu_record:
                if (actualId != RECORDER_ID)
                    goToActivity(RECORDER_ID, RecorderActivity.class);
                break;
            case R.id.menu_stats:
                if (actualId != STATISTICS_ID)
                    goToActivity(STATISTICS_ID, StatisticsActivity.class);
                break;
            case R.id.menu_shop:
                if (actualId != SHOP_ID)
                    goToActivity(SHOP_ID, ShopActivity.class);
                break;
            case R.id.menu_user:
                if (actualId != USER_ID)
                    goToActivity(USER_ID, ProfileActivity.class);
                break;
            case R.id.menu_settings:
                Toast.makeText(parent.getApplicationContext(), "SETTINGS", Toast.LENGTH_LONG).show();
                break;

        }
    }

    public void goToActivity(int startId, Class<?> cls){
        if (actualId != startId) {
            Intent intent = new Intent(parent, cls);
            parent.startActivity(intent);
        }
    }

}
