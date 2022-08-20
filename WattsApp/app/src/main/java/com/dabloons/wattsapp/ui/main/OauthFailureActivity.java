package com.dabloons.wattsapp.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.auth.OAuth2Manager;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenResponse;

public class OauthFailureActivity extends AppCompatActivity {

    private OAuth2Manager oauthManager = OAuth2Manager.getInstance();

    public OauthFailureActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthorizationResponse resp = AuthorizationResponse.fromIntent(getIntent());
        AuthorizationException ex = AuthorizationException.fromIntent(getIntent());
        oauthManager.getAuthState().update(resp, ex);
        if (resp != null) {
            // authorization completed
            Uri redirectUri = getIntent().getData();

            oauthManager.getAuthService().performTokenRequest(
                    resp.createTokenExchangeRequest(),
                    new AuthorizationService.TokenResponseCallback() {
                        @Override public void onTokenRequestCompleted(
                                TokenResponse resp, AuthorizationException ex) {
                            if (resp != null) {
                                // exchange succeeded
                            } else {
                                // authorization failed, check ex for more details
                            }
                        }
                    });
        } else {
            // authorization failed, check ex for more details
        }
    }
}