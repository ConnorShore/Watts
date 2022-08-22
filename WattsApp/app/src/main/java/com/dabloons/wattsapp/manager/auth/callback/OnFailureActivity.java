package com.dabloons.wattsapp.manager.auth.callback;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.dabloons.wattsapp.manager.auth.OAuthConnectionManager;
import com.dabloons.wattsapp.manager.auth.PhillipsHueOAuthManager;
import com.dabloons.wattsapp.model.IntegrationType;

public class OnFailureActivity extends AppCompatActivity {

    public OnFailureActivity() { }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntegrationType type = OAuthConnectionManager.getInstance().getCurrentConnection();
        switch(type) {
            case PHILLIPS_HUE:
                PhillipsHueOAuthManager.getInstance().handleAuthResponseFailure(getIntent());
                break;
            case NANOLEAF:
            default:
                Log.e("OnSuccessActivity", "Failed to get integration type");
        }
    }
}