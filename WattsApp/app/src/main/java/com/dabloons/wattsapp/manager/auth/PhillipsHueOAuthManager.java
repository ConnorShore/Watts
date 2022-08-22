package com.dabloons.wattsapp.manager.auth;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.repository.UserAuthRepository;
import com.dabloons.wattsapp.service.PhillipsHueService;
import com.dabloons.wattsapp.service.streams.PhillipsHueStreams;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ClientSecretBasic;
import net.openid.appauth.TokenResponse;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class PhillipsHueOAuthManager extends OAuthManager {

    private static volatile PhillipsHueOAuthManager instance;

    private static final UserAuthRepository userAuthRepository = UserAuthRepository.getInstance();

    // TODO: Move these to config
    private final String HUE_CLIENT_ID = "MHXlFaIMPE52gfnlM0nJ1fzYWOLe8fAv";
    private final String HUE_CLIENT_SECRET = "onksb0tv1Vz30vDl";

    private final Uri HUE_REDIRECT_URI = Uri.parse("com.dabloons.wattsapp:/oauth2redirect");
    private final Uri HUE_USERNAME_URI = Uri.parse("https://api.meethue.com/route/api/0/config");

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
            aquireTokens(resp);
        }
        else {
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

    /**
     * Helpers
     */

    private void aquireTokens(AuthorizationResponse response) {
        // authorization completed
        ClientAuthentication auth = new ClientSecretBasic(getClientSecret());
        this.getAuthService().performTokenRequest(response.createTokenExchangeRequest(), auth, (resp, ex1) -> {
            updateAuthState(resp, ex1);
            if (resp != null) {
                // exchange succeeded
                // TODO: Need to move the repository calls to managers
                // TODO: NEed to make sure database writes work
                // TODO: Move to OkHttp for web requests
                userAuthRepository.addPhillipsHueIntegrationToUser(resp.accessToken, resp.refreshToken);
//                String username = aquireUserName();
//                userAuthRepository.updatePropertyString("username", username, IntegrationType.PHILLIPS_HUE);
                this.endOauthConnection();
            } else {
                // authorization failed, check ex for more details
                System.out.println();
                this.endOauthConnection();
            }
        });
    }

    private String aquireUserName() {
        try {
            PhillipsHueStreams.linkButton().execute();
            String username = PhillipsHueStreams.getUsername().execute().body();
            return username;
        } catch (IOException e) {
            Log.e("PhillipsHueOAUthManager", "IO EXception getting username: " + e.getMessage());
        }
        return null;
    }

    /**
     * Overrides (and instance)
     */

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
