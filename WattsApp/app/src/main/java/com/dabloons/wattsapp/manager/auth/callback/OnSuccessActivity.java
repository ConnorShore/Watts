package com.dabloons.wattsapp.manager.auth.callback;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.dabloons.wattsapp.manager.auth.OAuthConnectionManager;
import com.dabloons.wattsapp.manager.auth.PhillipsHueOAuthManager;
import com.dabloons.wattsapp.model.integration.IntegrationType;

public class OnSuccessActivity extends AppCompatActivity {

    private final String LOG_TAG = "OnSuccessActivity";

    public OnSuccessActivity() { }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntegrationType type = OAuthConnectionManager.getInstance().getCurrentConnection();
        switch(type) {
            case PHILLIPS_HUE:
                PhillipsHueOAuthManager.getInstance().handleAuthResponseSuccess(getIntent());
                break;
            case NANOLEAF:
            default:
                Log.e(LOG_TAG, "Failed to get integration type");
        }
    }
}
