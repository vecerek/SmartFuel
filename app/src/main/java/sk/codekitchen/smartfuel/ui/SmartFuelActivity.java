package sk.codekitchen.smartfuel.ui;

import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.graphics.Typeface;
import android.content.SharedPreferences;
import android.os.Build;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.opengl.Visibility;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import java.util.Locale;

import sk.codekitchen.smartfuel.R;
import sk.codekitchen.smartfuel.service.GPSTrackerService;
import sk.codekitchen.smartfuel.ui.fragments.CustomViewPager;
import sk.codekitchen.smartfuel.ui.fragments.FragmentAdapter;
import sk.codekitchen.smartfuel.ui.fragments.FragmentProfile;
import sk.codekitchen.smartfuel.ui.fragments.FragmentRecorder;
import sk.codekitchen.smartfuel.ui.fragments.FragmentSettings;
import sk.codekitchen.smartfuel.ui.fragments.FragmentShop;
import sk.codekitchen.smartfuel.ui.fragments.FragmentStatistics;
import sk.codekitchen.smartfuel.ui.views.ExtraboldTextView;
import sk.codekitchen.smartfuel.ui.views.MenuTextItems;
import sk.codekitchen.smartfuel.ui.views.SemiboldTextView;
import sk.codekitchen.smartfuel.util.GLOBALS;

/**
 * @author Gabriel Lehocky
 */
public class SmartFuelActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    public final static int NOTIFICATION_RECORDING_ID = 0;
    private final static int FINE_LOCATION_RESULT = 100;

    /**
     * Android Marshmallow Permission Request instance variables.
     */
    private SharedPreferences sharedPreferences;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected;
    private View coordinatorLayoutView;

    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView menu;
    private ExtraboldTextView title;
    private ImageView button_rec;
    private ImageView button_stop;
    private ExtraboldTextView txt_rec;
    private ExtraboldTextView txt_stop;

    private boolean isRecording = false;

    private CustomViewPager viewPager;
    private FragmentRecorder fRecorder = new FragmentRecorder();
    private FragmentStatistics fStatistics = new FragmentStatistics();
    private FragmentShop fShop = new FragmentShop();
    private FragmentProfile fProfile = new FragmentProfile();
    private FragmentSettings fSettings = new FragmentSettings();

    private NotificationManager notificationManager;
    private PendingIntent stopRecorder;

    //GPSTrackerService mService;
    Messenger mGPSTrackerMessenger;
    boolean mBound = false;

    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Log.i("TEST_IPC", "Message getting handled");
            Bundle b = msg.getData();
            if (b != null) {
                Map<String, String> params = getParams(b);
                if (params != null) {
                    switch (msg.what) {
                        case GPSTrackerService.UPDATE_STATE:
                            break;
                        case GPSTrackerService.ERROR:
                            break;
                        case GPSTrackerService.DEBUG:
                            Log.d("TEST_IPC", "Response: " + b.getCharSequence("data"));
                            fRecorder.setSpeed(Integer.parseInt(params.get(GLOBALS.IPC_MESSAGE_KEY.SPEED)));
                            fRecorder.setPercent(Integer.parseInt(params.get(GLOBALS.IPC_MESSAGE_KEY.PROGRESS)));
                            fRecorder.setSpeedLimit(Integer.parseInt(params.get(GLOBALS.IPC_MESSAGE_KEY.LIMIT)));
                            break;
                        default:
                            super.handleMessage(msg);
                    }
                }
            }
        }

        private Map<String, String> getParams(Bundle b) {
            Map<String, String> mParams = new LinkedHashMap<>();
            String tParams = (String) b.getCharSequence("data");
            if (tParams != null) {
                for (String keyValue : tParams.split("&")) {
                    String[] pairs = keyValue.split("=", 2);
                    mParams.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
                }
                return mParams;
            }
            return null;
        }
    }

    /**
     * Messenger used for receiving responses from GPSTrackerService.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());


    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i("TEST_IPC", "onServiceConnected triggered");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            //GPSTrackerService.GPSTrackerBinder binder = (GPSTrackerService.GPSTrackerBinder) service;
            mGPSTrackerMessenger = new Messenger(service);
            //mService = binder.getService();
            mBound = true;
            Log.i("TEST_IPC", "Service's Messenger saved");

            // Register our messenger also on Service side:
            Message msg = Message.obtain(null, GPSTrackerService.REGISTER);
            msg.replyTo = mMessenger;

            try {
                Log.i("TEST_IPC", "Sending REGISTER message to the service");
                mGPSTrackerMessenger.send(msg);
            } catch (RemoteException e) {
                // We always have to trap RemoteException
                // (DeadObjectException
                // is thrown if the target Handler no longer exists)
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_fuel);

        initLayout();

        //for determining if we have asked the questions..
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        coordinatorLayoutView = findViewById(R.id.drawer_layout);

    }

    @Override
    protected void onStart() {
        Log.i("TEST_IPC", "onStart triggered");
        super.onStart();
        // Bind to LocalService
        if (mBound) {
            bindToGPSTrackerService();
        }
    }

    @Override
    protected void onStop() {
        Log.i("TEST_IPC", "onStop triggered");
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
        }
    }

    protected void bindToGPSTrackerService() {
        Log.i("TEST_IPC", "Binding to the GPSTrackerService");
        Intent intent = new Intent(this, GPSTrackerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;
    }

    protected void stopGPSTrackerService() {
        Log.i("TEST_IPC", "Stopping GPSTrackerService");
        unbindService(mConnection);
        stopService(new Intent(this, GPSTrackerService.class));
        mBound = false;
    }

    private void initLayout(){
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        title = (ExtraboldTextView) findViewById(R.id.toolbar_title);
        button_rec = (ImageView) findViewById(R.id.toolbar_rec);
        button_rec.setOnClickListener(this);
        button_stop = (ImageView) findViewById(R.id.toolbar_stop);
        button_stop.setOnClickListener(this);
        txt_rec = (ExtraboldTextView) findViewById(R.id.toolbar_rec_txt);
        txt_rec.setOnClickListener(this);
        txt_stop = (ExtraboldTextView) findViewById(R.id.toolbar_stop_txt);
        txt_stop.setOnClickListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        createMenu();

        final FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        addFragments(adapter);

        viewPager = (CustomViewPager) findViewById(R.id.pager_sf);
        viewPager.setAdapter(adapter);
        viewPager.setPagingEnabled(false);

        SharedPreferences preferences;
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setLocale(preferences.getString(GLOBALS.SETTINGS_LANG, ""));

    }

    private void createMenu(){
        menu = (NavigationView) findViewById(R.id.nav_view);
        menu.setNavigationItemSelectedListener(this);

        Menu m = menu.getMenu();
        for (int i = 0; i < m.size() ; i++) {
            MenuItem mi = m.getItem(i);
            applyFontToMenuItem(mi);
        }
    }

    private void addFragments(FragmentAdapter adapter){
        adapter.addFragment(fRecorder);
        adapter.addFragment(fStatistics);
        adapter.addFragment(fShop);
        adapter.addFragment(fProfile);
        adapter.addFragment(fSettings);
    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "Fonts/ProximaNova-Light.otf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new MenuTextItems("" , font), 0 , mNewTitle.length(),  Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (viewPager.getCurrentItem() != 0){
            viewPager.setCurrentItem(0, false);
            menu.setCheckedItem(R.id.nav_recorder);
            toolbarIcons(true);
        }
        else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        title.setText(item.getTitle().toString());

        if (id == R.id.nav_recorder) {
            viewPager.setCurrentItem(0, false);
            toolbarIcons(true);
        }
        else if (id == R.id.nav_stat) {
            viewPager.setCurrentItem(1, false);
            fStatistics.loadUnits();
            toolbarIcons(false);
        }
        else if (id == R.id.nav_shop) {
            viewPager.setCurrentItem(2, false);
            toolbarIcons(false);
        }
        else if (id == R.id.nav_profile) {
            viewPager.setCurrentItem(3, false);
            fProfile.loadUnits();
            toolbarIcons(false);
        }
        else if (id == R.id.nav_settings) {
            viewPager.setCurrentItem(4, false);
            toolbarIcons(false);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @TargetApi(23)
    @Override
    public void onClick(View v) {
        ArrayList<String> permissions = new ArrayList<>();
        int resultCode = 0;

        switch (v.getId()){
            case R.id.toolbar_rec_txt:
            case R.id.toolbar_rec:
                permissions.add(ACCESS_FINE_LOCATION);
                resultCode = FINE_LOCATION_RESULT;
                break;
            case R.id.toolbar_stop_txt:
            case R.id.toolbar_stop:
                isRecording = false;
                destroyNotification();
                toolbarIcons(true);
                stopGPSTrackerService();
                break;
        }

        if (resultCode != 0) {
            checkRecordingPermissions(permissions, resultCode);
        }
    }

    private void toolbarIcons(boolean show) {
        if (show) {
            if (isRecording) {
                button_rec.setVisibility(View.GONE);
                button_stop.setVisibility(View.VISIBLE);
                txt_rec.setVisibility(View.GONE);
                txt_stop.setVisibility(View.VISIBLE);

            } else {
                button_rec.setVisibility(View.VISIBLE);
                button_stop.setVisibility(View.GONE);
                txt_rec.setVisibility(View.VISIBLE);
                txt_stop.setVisibility(View.GONE);
            }

        } else {
            button_rec.setVisibility(View.GONE);
            button_stop.setVisibility(View.GONE);
            txt_rec.setVisibility(View.GONE);
            txt_stop.setVisibility(View.GONE);
        }
    }

    /**
     * HELP:
     *      http://developer.android.com/training/notify-user/expanded.html
     *      http://developer.android.com/guide/topics/ui/notifiers/notifications.html
     */

    private void createNotification() {

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder newNotification =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.speed)
                .setContentTitle(getString(R.string.notification_title))
                .addAction(R.drawable.ic_notification, getString(R.string.toolbar_stop), stopRecorder)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        notificationManager.notify(NOTIFICATION_RECORDING_ID, newNotification.build());
    }

    private void startRecording() {
        isRecording = true;
        createNotification();
        toolbarIcons(true);
        startService(new Intent(this, GPSTrackerService.class));
        bindToGPSTrackerService();
    }

    private void destroyNotification() {
        notificationManager.cancel(NOTIFICATION_RECORDING_ID);
    }


    public void setLocale(String lang) {
        if (lang.equals("")) return;
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, SmartFuelActivity.class);
        startActivity(refresh);

        if (viewPager.getCurrentItem() == 4){
            viewPager.setCurrentItem(0, false);
            menu.setCheckedItem(R.id.nav_recorder);
        }

    }

    @TargetApi(23)
    private void checkRecordingPermissions(ArrayList<String> permissions, int resultCode) {
        //filter out the permissions we have already accepted
        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.
        permissionsRejected = findRejectedPermissions(permissions);

        if (permissionsToRequest.size() > 0) { //we need to ask for permissions
            //but have we already asked for them?
            requestPermissions(permissionsToRequest
                    .toArray(new String[permissionsToRequest.size()]), resultCode);
            //mark all these as asked..
            for(String perm : permissionsToRequest) {
                markAsAsked(perm);
            }

        } else {
            //show the success banner
            if (permissionsRejected.size() < permissions.size()) {
                //this means we can show success because some were already accepted.
                startRecording();
            }

            if (permissionsRejected.size() > 0) {
                //we have none to request but some previously rejected..tell the user.
                //It may be better to show a dialog here in a prod application
                Snackbar
                        .make(coordinatorLayoutView, String.valueOf(permissionsRejected.size()) +
                                " permission(s) were previously rejected", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Allow to Ask Again", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for (String perm: permissionsRejected) {
                                    clearMarkAsAsked(perm);
                                }
                            }
                        })
                        .show();
            }
        }
    }

    /**
     * This is the method that is hit after the user accepts/declines the
     * permission you requested. For the purpose of this example I am showing a "success" header
     * when the user accepts the permission and a snackbar when the user declines it.  In your
     * application you will want to handle the accept/decline in a way that makes sense.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case FINE_LOCATION_RESULT:
                if (hasPermission(ACCESS_FINE_LOCATION)) {
                    startRecording();
                } else {
                    permissionsRejected.add(ACCESS_FINE_LOCATION);
                    makePostRequestSnack();
                }
                break;
        }

    }

    /**
     * a method that will centralize the showing of a snackbar
     */
    private void makePostRequestSnack() {
        Snackbar
                .make(coordinatorLayoutView, String.valueOf(permissionsRejected.size()) +
                        " permission(s) were rejected", Snackbar.LENGTH_LONG)
                .setAction("Allow to Ask Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (String perm: permissionsRejected) {
                            clearMarkAsAsked(perm);
                        }
                    }
                })
                .show();
    }

    /**
     * method that will return whether the permission is accepted. By default it is true if the user
     * is using a device below version 23
     * @param permission
     * @return
     */
    @TargetApi(23)
    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            return(checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    /**
     * method to determine whether we have asked
     * for this permission before.. if we have, we do not want to ask again.
     * They either rejected us or later removed the permission.
     * @param permission
     * @return
     */
    private boolean shouldWeAsk(String permission) {
        return(sharedPreferences.getBoolean(permission, true));
    }

    /**
     * we will save that we have already asked the user
     * @param permission
     */
    private void markAsAsked(String permission) {
        sharedPreferences.edit().putBoolean(permission, false).apply();
    }

    /**
     * We may want to ask the user again at their request.. Let's clear the
     * marked as seen preference for that permission.
     * @param permission
     */
    private void clearMarkAsAsked(String permission) {
        sharedPreferences.edit().putBoolean(permission, true).apply();
    }


    /**
     * This method is used to determine the permissions we do not have accepted yet and ones that we
     * have not already bugged the user about.  This comes in handle when you are asking for multiple
     * permissions at once.
     * @param wanted
     * @return
     */
    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wanted) {
            if (!hasPermission(perm) && shouldWeAsk(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    /**
     * this will return us all the permissions we have previously asked for but
     * currently do not have permission to use. This may be because they declined us
     * or later revoked our permission. This becomes useful when you want to tell the user
     * what permissions they declined and why they cannot use a feature.
     * @param wanted
     * @return
     */
    private ArrayList<String> findRejectedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wanted) {
            if (!hasPermission(perm) && !shouldWeAsk(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    /**
     * Just a check to see if we have marshmallows (version 23)
     * @return
     */
    private boolean canMakeSmores() {
        return(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }
}
