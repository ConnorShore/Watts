package com.dabloons.wattsapp;

import android.app.Application;
import android.content.Context;

public class WattsApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        WattsApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return WattsApplication.context;
    }
}
