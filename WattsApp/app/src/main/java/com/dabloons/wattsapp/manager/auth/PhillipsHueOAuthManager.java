package com.dabloons.wattsapp.manager.auth;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.repository.UserAuthRepository;
import com.dabloons.wattsapp.service.PhillipsHueService;
import com.dabloons.wattsapp.ui.main.MainActivity;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ClientSecretBasic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PhillipsHueOAuthManager extends OAuthManager {

    private final String LOG_TAG = "PhillipsHueOAuthManager";

    private static volatile PhillipsHueOAuthManager instance;

    private static final UserAuthRepository userAuthRepository = UserAuthRepository.getInstance();
    private static final PhillipsHueService phillipsHueService = PhillipsHueService.getInstance();

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
            aquireTokens(resp, intent);
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

    private void aquireTokens(AuthorizationResponse response, Intent intent) {
        // authorization completed
        ClientAuthentication auth = new ClientSecretBasic(getClientSecret());
        this.getAuthService().performTokenRequest(response.createTokenExchangeRequest(), auth, (resp, ex1) -> {
            updateAuthState(resp, ex1);
            if (resp != null) {
                // exchange succeeded
                String accessToken = resp.accessToken;
                String refreshToken = resp.refreshToken;
                aquireUserNameAndSaveData(accessToken, refreshToken, intent);
            } else {
                // authorization failed, check ex for more details
                System.out.println();
                this.endOauthConnection();
            }
        });
    }

    private void aquireUserNameAndSaveData(String accessToken, String refreshToken, Intent intent) {

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
                        try {
                            // Todo: Move to JsonArray and JsonObject (gson)
                            JSONArray jsonObj = new JSONArray(responseData);
                            JSONObject successObj = jsonObj.getJSONObject(0);
                            String username = successObj.getJSONObject("success").getString("username");
                            userAuthRepository.addPhillipsHueIntegrationToUser(accessToken, refreshToken, username)
                                    .addOnSuccessListener(v -> {
                                        endOauthConnection();
                                        launchMainActivity();
                                    })
                                    .addOnFailureListener(v -> {

                                    });
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, "Failed to decode json response: " + e.getMessage());
                        }
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

    private void launchMainActivity() {
        Context context = WattsApplication.getAppContext();
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }
}
