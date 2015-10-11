package sk.codekitchen.smartfuel.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Vector;


import sk.codekitchen.smartfuel.R;
import sk.codekitchen.smartfuel.model.SFDB;
import sk.codekitchen.smartfuel.model.User;
import sk.codekitchen.smartfuel.ui.GUI.CustomViewPager;
import sk.codekitchen.smartfuel.ui.GUI.EditLightTextView;
import sk.codekitchen.smartfuel.ui.GUI.FragmentAdapter;
import sk.codekitchen.smartfuel.ui.GUI.LightTextView;
import sk.codekitchen.smartfuel.ui.GUI.Utils;
import sk.codekitchen.smartfuel.util.ConnectionManager;
import sk.codekitchen.smartfuel.util.GLOBALS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

	private static final int MIN_EMAIL_LEGTH = 5;
	private static final int MIN_PASSWORD_LEGTH = 5;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private boolean isLoggedIn = false;
    protected MainActivity same = this;
    protected Vector<Integer> dots = new Vector<>();

    private Button login;
    private EditLightTextView mail;
    private EditLightTextView pass;
    private LightTextView forgotten;
    private LightTextView register;
	private ProgressDialog mProgressView;
	private TabLayout tabLayout;
	private LinearLayout loginScreen;
	private ImageView splashScreen;

	private CustomViewPager viewPager;

	private static final int SPLASH_TIME_OUT = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setView();
	    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    isLoggedIn = preferences.getInt(GLOBALS.USER_ID, -1) != -1;

        if (isLoggedIn){
			goToRecorderActivity();
        }
    }

    private void setView(){
        dots.add(R.id.intro_dot_0);
        dots.add(R.id.intro_dot_1);
        dots.add(R.id.intro_dot_2);

		splashScreen = (ImageView) findViewById(R.id.intro_logo);
		loginScreen = (LinearLayout) findViewById(R.id.login_screen);

        login = (Button) findViewById(R.id.login_btn);
        login.setOnClickListener(this);
        mail = (EditLightTextView) findViewById(R.id.login_mail);
        pass = (EditLightTextView) findViewById(R.id.login_pass);
        register = (LightTextView) findViewById(R.id.login_register);
        register.setOnClickListener(this);
        forgotten = (LightTextView) findViewById(R.id.login_forgotten);
        forgotten.setOnClickListener(this);

	    mProgressView = new ProgressDialog(this);
	    mProgressView.setTitle(getString(R.string.login_progress_title));
	    mProgressView.setMessage(getString(R.string.login_progress_msg));

		tabLayout = (TabLayout) findViewById(R.id.tab_layout);
		tabLayout.addTab(tabLayout.newTab().setText("1"));
		tabLayout.addTab(tabLayout.newTab().setText("2"));
		tabLayout.addTab(tabLayout.newTab().setText("3"));
		tabLayout.addTab(tabLayout.newTab().setText("Login"));
		tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

		viewPager = (CustomViewPager) findViewById(R.id.pager);
		final FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
		viewPager.setAdapter(adapter);
		viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
		tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				viewPager.setCurrentItem(tab.getPosition());

				View dot;

				for (Integer d : dots) {
					dot = findViewById(d);
					Utils.setBackgroundOfView(same, dot, R.drawable.dot_white);
				}

				switch (tab.getPosition()){
					case 0:
					case 1:
					case 2:
						dot = findViewById(dots.elementAt(tab.getPosition()));
						Utils.setBackgroundOfView(same, dot, R.drawable.dot_color);
						break;
					case 3:
						viewPager.setVisibility(View.GONE);
						LinearLayout dotLayout = (LinearLayout) findViewById(R.id.intro_dots);
						dotLayout.setVisibility(View.GONE);
						break;
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

	public void displaySplashScreen(){
		viewPager.setCurrentItem(3);
		loginScreen.setVisibility(View.INVISIBLE);
		splashScreen.setVisibility(View.VISIBLE);
	}

    @Override
	protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_btn:
	            attemptToLogin();
                break;
            case R.id.login_forgotten:
                break;
            case R.id.login_register:
                break;
        }
    }

	private void goToRecorderActivity() {
		displaySplashScreen();
		(new SyncDatabaseTask()).execute((Void) null);
	}

	public void attemptToLogin() {
		if (mAuthTask != null)
			return;

		String email = mail.getText().toString();
		String password = pass.getText().toString();

		View focusView = null;

		// Check for a valid password, if the user entered one.
		if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
			pass.setError(getString(R.string.error_invalid_password));
			focusView = pass;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(email)) {
			mail.setError(getString(R.string.error_field_required));
			focusView = mail;
		} else if (!isEmailValid(email)) {
			mail.setError(getString(R.string.error_invalid_email));
			focusView = mail;
		}

		if (focusView != null) {
			focusView.requestFocus();
		} else {
			mAuthTask = new UserLoginTask(email, password);
			mAuthTask.execute((Void) null);
			showProgress(true);
		}
	}

	private boolean isEmailValid(String email) {
		return email.length() >= MIN_EMAIL_LEGTH;
	}

	private boolean isPasswordValid(String pass) {
		return pass.length() >= MIN_PASSWORD_LEGTH;
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	public void showProgress(final boolean show) {

		// if progress is active, disable fields, otherwise enable them
		mail.setEnabled(!show);
		pass.setEnabled(!show);

		if(show) mProgressView.show();
		else mProgressView.dismiss();
	}

	/**
	 * Represents an asynchronous login task used to authenticate
	 * the user.
	 */
	private class UserLoginTask extends AsyncTask<Void, Void, Integer> {

		private final String mEmail;
		private final String mPassword;
		private String error = null;

		UserLoginTask(String email, String password) {
			mEmail = email;
			mPassword = password;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			Integer userID = null;
			try {
				userID = User.authenticate(mEmail, mPassword);
			} catch (Exception e) {
				error = e.getMessage();
			}

			return userID;
		}

		@Override
		protected void onPostExecute(final Integer userID) {
			mAuthTask = null;
			showProgress(false);

			if (userID != null) {
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext());
				prefs.edit().putInt(GLOBALS.USER_ID, userID).apply();

				goToRecorderActivity();
			} else {
				switch (error) {
					case GLOBALS.BAD_EMAIL:
						mail.setError(getString(R.string.error_account_not_registered));
						mail.requestFocus();
						break;
					case GLOBALS.BAD_PASS:
						pass.setError(getString(R.string.error_incorrect_password));
						pass.requestFocus();
						break;
					default:
						break;
				}
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}

	private class SyncDatabaseTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				ConnectionManager con = new ConnectionManager(getApplicationContext());
				if (con.isConnectionFast()) {
					(new SFDB(getApplicationContext())).sync();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void param) {

			try {
				Thread.sleep(SPLASH_TIME_OUT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Intent intent = new Intent(MainActivity.this, RecorderActivity.class);

			startActivity(intent);
			finish();
		}
	}
}

