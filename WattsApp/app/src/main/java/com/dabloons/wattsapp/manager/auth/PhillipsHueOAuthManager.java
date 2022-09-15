package com.dabloons.wattsapp.manager.auth;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.model.integration.PhillipsHueIntegrationAuth;
import com.dabloons.wattsapp.repository.UserAuthRepository;
import com.dabloons.wattsapp.service.PhillipsHueService;
import com.dabloons.wattsapp.ui.main.MainActivity;

import com.google.gson.*;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ClientSecretBasic;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class PhillipsHueOAuthManager extends OAuthManager {

    private final String LOG_TAG = "PhillipsHueOAuthManager";

    private static volatile PhillipsHueOAuthManager instance;

    private final UserManager userManager = UserManager.getInstance();
    private final PhillipsHueService phillipsHueService = PhillipsHueService.getInstance();

    private final String HUE_CLIENT_ID = WattsApplication.getResourceString(R.string.hue_client_id);
    private final String HUE_CLIENT_SECRET = WattsApplication.getResourceString(R.string.hue_client_secret);

    private final Uri HUE_REDIRECT_URI = Uri.parse(WattsApplication.getResourceString(R.string.hue_redirect_uri));;

    public PhillipsHueOAuthManager() {
        super(WattsApplication.getResourceString(R.string.hue_authorize_endpoint), WattsApplication.getResourceString(R.string.hue_token_endpoint));
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
            Log.e(LOG_TAG, ex.error);
            this.endOauthConnection();
        }
    }

    @Override
    public void handleAuthResponseFailure(Intent intent) {
        AuthorizationResponse resp = AuthorizationResponse.fromIntent(intent);
        AuthorizationException ex = AuthorizationException.fromIntent(intent);
        updateAuthState(resp, ex);
        Log.e(LOG_TAG, "OAuth Response Failed: " + ex.error);
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
                String accessToken = resp.accessToken;
                String refreshToken = resp.refreshToken;
                aquireUserNameAndSaveData(accessToken, refreshToken);
            } else {
                // authorization failed, check ex for more details
                System.out.println();
                this.endOauthConnection();
            }
        });
    }

    private void aquireUserNameAndSaveData(String accessToken, String refreshToken) {

        phillipsHueService.linkButton(accessToken, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(LOG_TAG, "Failed to link button: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                phillipsHueService.getUsername(accessToken, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e(LOG_TAG, "Failed to get username: " + e.getMessage());
                        endOauthConnection();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String responseData = response.body().string();
                        JsonArray jsonObj = JsonParser.parseString(responseData).getAsJsonArray();
                        JsonObject successObj = jsonObj.get(0).getAsJsonObject();
                        String username = successObj.get("success").getAsJsonObject().get("username").getAsString();
                        PhillipsHueIntegrationAuth authData = new PhillipsHueIntegrationAuth(username, accessToken, refreshToken);
                        userManager.addIntegrationAuthData(IntegrationType.PHILLIPS_HUE, authData, (var, status) -> {
                            if(!status.success)
                                Log.e(LOG_TAG, status.message);

                            endOauthConnection();
                            launchMainActivity();
                        });
                    }
                });
            }
        });
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

    private void launchMainActivity() {
        Context context = WattsApplication.getAppContext();
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("fragToLoad", 2);
        context.startActivity(intent);
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
