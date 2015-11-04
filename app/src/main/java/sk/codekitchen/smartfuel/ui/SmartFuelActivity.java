package sk.codekitchen.smartfuel.ui;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import sk.codekitchen.smartfuel.R;
import sk.codekitchen.smartfuel.ui.fragments.CustomViewPager;
import sk.codekitchen.smartfuel.ui.fragments.FragmentAdapter;
import sk.codekitchen.smartfuel.ui.fragments.FragmentProfile;
import sk.codekitchen.smartfuel.ui.fragments.FragmentRecorder;
import sk.codekitchen.smartfuel.ui.fragments.FragmentSettings;
import sk.codekitchen.smartfuel.ui.fragments.FragmentShop;
import sk.codekitchen.smartfuel.ui.fragments.FragmentStatistics;
import sk.codekitchen.smartfuel.ui.views.ExtraboldTextView;

/**
 * @author Gabriel Lehocky
 */
public class SmartFuelActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView menu;
    private ExtraboldTextView title;

    private CustomViewPager viewPager;
    FragmentRecorder fRecorder = new FragmentRecorder();
    FragmentStatistics fStatistics = new FragmentStatistics();
    FragmentShop fShop = new FragmentShop();
    FragmentProfile fProfile = new FragmentProfile();
    FragmentSettings fSettings = new FragmentSettings();

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

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        menu = (NavigationView) findViewById(R.id.nav_view);
        menu.setNavigationItemSelectedListener(this);

        final FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(fRecorder);
        adapter.addFragment(fStatistics);
        adapter.addFragment(fShop);
        adapter.addFragment(fProfile);
        adapter.addFragment(fSettings);

        viewPager = (CustomViewPager) findViewById(R.id.pager_sf);
        viewPager.setAdapter(adapter);
        viewPager.setPagingEnabled(false);

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (viewPager.getCurrentItem() != 0){
            viewPager.setCurrentItem(0, false);
        }
        else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        title.setText(item.getTitle());

        if (id == R.id.nav_recorder) {
            viewPager.setCurrentItem(0, false);
        }
        else if (id == R.id.nav_stat) {
            viewPager.setCurrentItem(1, false);
        }
        else if (id == R.id.nav_shop) {
            viewPager.setCurrentItem(2, false);
        }
        else if (id == R.id.nav_profile) {
            viewPager.setCurrentItem(3, false);
        }
        else if (id == R.id.nav_settings) {
            viewPager.setCurrentItem(4, false);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
