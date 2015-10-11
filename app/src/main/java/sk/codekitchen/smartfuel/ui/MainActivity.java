package sk.codekitchen.smartfuel.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Vector;

import sk.codekitchen.smartfuel.R;
import sk.codekitchen.smartfuel.ui.GUI.EditLightTextView;
import sk.codekitchen.smartfuel.ui.GUI.FragmentAdapter;
import sk.codekitchen.smartfuel.ui.GUI.LightTextView;
import sk.codekitchen.smartfuel.ui.GUI.Utils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public final static int LOGIN_TAB_ID = 3;

    private boolean isLoggedIn = false;
    protected MainActivity same = this;
    protected Vector<Integer> dots = new Vector<>();

    private Button login;
    private EditLightTextView mail;
    private EditLightTextView pass;
    private LightTextView forgotten;
    private LightTextView register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setView();

        if (!isLoggedIn){
            showIntro();
        }
        else{
            Intent intent = new Intent(this, RecorderActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void setView(){
        dots.add(R.id.intro_dot_0);
        dots.add(R.id.intro_dot_1);
        dots.add(R.id.intro_dot_2);

        login = (Button) findViewById(R.id.login_btn);
        login.setOnClickListener(this);
        mail = (EditLightTextView) findViewById(R.id.login_mail);
        pass = (EditLightTextView) findViewById(R.id.login_pass);
        register = (LightTextView) findViewById(R.id.login_register);
        register.setOnClickListener(this);
        forgotten = (LightTextView) findViewById(R.id.login_forgotten);
        forgotten.setOnClickListener(this);

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

                if (tab.getPosition() < LOGIN_TAB_ID){
                    dot = findViewById(dots.elementAt(tab.getPosition()));
                    Utils.setBackgroundOfView(same, dot, R.drawable.dot_color);
                }
                else{
                    viewPager.setVisibility(View.GONE);
                    LinearLayout dotLayout = (LinearLayout) findViewById(R.id.intro_dots);
                    dotLayout.setVisibility(View.GONE);
                }

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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_btn:
                break;
            case R.id.login_forgotten:
                break;
            case R.id.login_register:
                break;
        }
    }
}

