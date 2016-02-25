package sk.codekitchen.smartfuel.ui;

import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.opengl.Visibility;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
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

import java.util.Locale;

import sk.codekitchen.smartfuel.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_fuel);

        initLayout();

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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.toolbar_rec_txt:
            case R.id.toolbar_rec:
                isRecording = true;
                createNotification();
                toolbarIcons(true);
                break;
            case R.id.toolbar_stop_txt:
            case R.id.toolbar_stop:
                isRecording = false;
                destroyNotification();
                toolbarIcons(true);
                break;
        }
    }

    private void toolbarIcons(boolean show){
        if(show){
            if(isRecording){
                button_rec.setVisibility(View.GONE);
                button_stop.setVisibility(View.VISIBLE);
                txt_rec.setVisibility(View.GONE);
                txt_stop.setVisibility(View.VISIBLE);
            }
            else {
                button_rec.setVisibility(View.VISIBLE);
                button_stop.setVisibility(View.GONE);
                txt_rec.setVisibility(View.VISIBLE);
                txt_stop.setVisibility(View.GONE);
            }
        }
        else {
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

    private void createNotification(){

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder newNotification =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.speed)
                .setContentTitle(getString(R.string.notification_title))
                .addAction(R.drawable.ic_notification, getString(R.string.toolbar_stop), stopRecorder)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        notificationManager.notify(NOTIFICATION_RECORDING_ID, newNotification.build());
    }

    private void destroyNotification(){
        notificationManager.cancel(NOTIFICATION_RECORDING_ID);
    }

}
