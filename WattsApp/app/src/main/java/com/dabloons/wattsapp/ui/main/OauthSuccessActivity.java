package com.dabloons.wattsapp.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.auth.OAuth2Manager;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ClientSecretBasic;
import net.openid.appauth.TokenResponse;

public class OauthSuccessActivity extends AppCompatActivity {

    private OAuth2Manager oauthManager = OAuth2Manager.getInstance();

    private String hueClientSecret = "onksb0tv1Vz30vDl";

    public OauthSuccessActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_oauth_success);

        AuthorizationResponse resp = AuthorizationResponse.fromIntent(getIntent());
        AuthorizationException ex = AuthorizationException.fromIntent(getIntent());
        oauthManager.getAuthState().update(resp, ex);
        if (resp != null) {
            // authorization completed
            Uri redirectUri = getIntent().getData();
            ClientAuthentication auth = new ClientSecretBasic(hueClientSecret);
            oauthManager.getAuthService().performTokenRequest(
                    resp.createTokenExchangeRequest(),
                    auth,
                    (resp1, ex1) -> {
                        oauthManager.getAuthState().update(resp1, ex1);
                        if (resp1 != null) {
                            // exchange succeeded
                            System.out.println();
                        } else {
                            // authorization failed, check ex for more details
                            System.out.println();
                        }
                    });
        } else {
            // authorization failed, check ex for more details
            System.out.println();
        }
    }
}