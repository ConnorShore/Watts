package com.dabloons.wattsapp.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.LightManager;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.ui.main.MainActivity;

import util.RequestCodes;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class SplashScreenActivity extends AppCompatActivity {

    private final String LOG_TAG = "SplashScreenActivity";

    private final int SPLASH_SCREEN_ACTIVE_TIME_MS = Integer.parseInt(WattsApplication.getResourceString(R.string.splash_screen_time));

    private final String SHARED_PREFERENCE_LOGIN = WattsApplication.getResourceString(R.string.shared_preference_login);

    public SplashScreenActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(UserManager.getInstance().getCurrentUser() != null) {
            LightManager.getInstance().syncLightsWithCallback((var, status) -> {
                startNextActivity();
                return null;
            });
        }
        else {
            Thread background = new Thread(() -> {
                try {
                    Thread.sleep(SPLASH_SCREEN_ACTIVE_TIME_MS);
                    startNextActivity();
                }
                catch (Exception ex) {
                    Log.e(LOG_TAG, ex.getMessage());
                }
            });

            background.start();
        }
    }

    private void startNextActivity() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_LOGIN, 0);
        boolean isLoggedIn = sharedPref.getBoolean(getString(R.string.logged_in), false);
        if (isLoggedIn) {
            Intent mainActivity = new Intent(SplashScreenActivity.this, MainActivity.class);
            startActivityForResult(mainActivity, RequestCodes.RC_MAIN_ACTIVITY);
        } else {
            Intent loginActivity = new Intent(SplashScreenActivity.this, LoginActivity.class);
            startActivityForResult(loginActivity, RequestCodes.RC_LOGIN_ACTIVITY);
        }
        finish();
    }
}
