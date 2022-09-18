package com.dabloons.wattsapp;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

public class WattsApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        WattsApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return WattsApplication.context;
    }

    public static String getResourceString(int resId) {
        return context.getString(resId);
    }

    public static int getColorInt(int resId) {
        return context.getColor(resId);
    }
}
