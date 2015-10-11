package sk.codekitchen.smartfuel.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.Vector;

import sk.codekitchen.smartfuel.R;
import sk.codekitchen.smartfuel.ui.GUI.FragmentAdapter;
import sk.codekitchen.smartfuel.ui.GUI.Utils;

public class MainActivity extends AppCompatActivity {

    private boolean isLoggedIn = false;
    protected MainActivity same = this;
    protected Vector<Integer> dots = new Vector<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dots.add(R.id.intro_dot_0);
        dots.add(R.id.intro_dot_1);
        dots.add(R.id.intro_dot_2);
        dots.add(R.id.intro_dot_3);

        if (!isLoggedIn){
            showIntro();
        }
        else{
            Intent intent = new Intent(this, RecorderActivity.class);
            startActivity(intent);
            finish();
        }
        
    }

    private void showIntro(){
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("1"));
        tabLayout.addTab(tabLayout.newTab().setText("2"));
        tabLayout.addTab(tabLayout.newTab().setText("3"));
        tabLayout.addTab(tabLayout.newTab().setText("Login"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                View dot;

                for (Integer d : dots){
                    dot = findViewById(d);
                    Utils.setBackgroundOfView(same, dot, R.drawable.dot_white);
                }

                dot = findViewById(dots.elementAt(tab.getPosition()));
                Utils.setBackgroundOfView(same, dot, R.drawable.dot_color);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

}

