package com.dabloons.wattsapp.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.ui.main.MainActivity;

import util.RequestCodes;

public class SplashScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread background = new Thread() {
            public void run()
            {
                try {
                    sleep(1500);
                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("TEST", 0);
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
                catch (Exception ex)
                {

                }

            }


        };
        background.start();


    }
}
