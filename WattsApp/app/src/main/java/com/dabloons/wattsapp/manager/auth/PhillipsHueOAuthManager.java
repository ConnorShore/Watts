package com.dabloons.wattsapp.manager.auth;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.dabloons.wattsapp.model.IntegrationType;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ClientSecretBasic;

public class PhillipsHueOAuthManager extends OAuthManager {

    private static volatile PhillipsHueOAuthManager instance;

    // TODO: Move these to config
    private final String HUE_CLIENT_ID = "MHXlFaIMPE52gfnlM0nJ1fzYWOLe8fAv";
    private final String HUE_CLIENT_SECRET = "onksb0tv1Vz30vDl";
    private final Uri HUE_REDIRECT_URI = Uri.parse("com.dabloons.wattsapp:/oauth2redirect");

    public PhillipsHueOAuthManager() {
        // TODO: MAke config
        super("https://api.meethue.com/v2/oauth2/authorize", "https://api.meethue.com/v2/oauth2/token");
    }

    @Override
    public void handleAuthResponseSuccess(Intent intent) {
        AuthorizationResponse resp = AuthorizationResponse.fromIntent(intent);
        AuthorizationException ex = AuthorizationException.fromIntent(intent);
        updateAuthState(resp, ex);
        if (resp != null) {
            // authorization completed
            Uri redirectUri = intent.getData();
            ClientAuthentication auth = new ClientSecretBasic(getClientSecret());
            this.getAuthService().performTokenRequest(
                    resp.createTokenExchangeRequest(),
                    auth,
                    (resp1, ex1) -> {
                        updateAuthState(resp1, ex1);
                        if (resp1 != null) {
                            // exchange succeeded
                            System.out.println();
                        } else {
                            // authorization failed, check ex for more details
                            System.out.println();
                        }
                        this.endOauthConnection();
                    });
        } else {
            // authorization failed, check ex for more details
            System.out.println();
            this.endOauthConnection();
        }
    }

    @Override
    public void handleAuthResponseFailure(Intent intent) {
        AuthorizationResponse resp = AuthorizationResponse.fromIntent(intent);
        AuthorizationException ex = AuthorizationException.fromIntent(intent);
        updateAuthState(resp, ex);
        Log.e("PhillipsHueOAuthManager", "OAuth Response Failed: " + ex.error);
    }

    @Override
    public String getClientID() {
        return this.HUE_CLIENT_ID;
    }

    @Override
    public String getClientSecret() {
        return this.HUE_CLIENT_SECRET;
    }

    @Override
    public Uri getRedirectUri() {
        return this.HUE_REDIRECT_URI;
    }

    @Override
    public IntegrationType getIntegrationType() {
        return IntegrationType.PHILLIPS_HUE;
    }

    public static PhillipsHueOAuthManager getInstance() {
        PhillipsHueOAuthManager result = instance;
        if (result != null) {
            return result;
        }
        synchronized(PhillipsHueOAuthManager.class) {
            if (instance == null) {
                instance = new PhillipsHueOAuthManager();
            }
            return instance;
        }
    }
}
