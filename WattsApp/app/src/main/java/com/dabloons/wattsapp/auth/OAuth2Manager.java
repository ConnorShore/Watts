package com.dabloons.wattsapp.auth;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.dabloons.wattsapp.ui.main.OauthFailureActivity;
import com.dabloons.wattsapp.ui.main.OauthSuccessActivity;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;


public class OAuth2Manager {

    private static volatile OAuth2Manager instance;

    private static final int RC_AUTH = 100;

    private String hueAuthEndpoint = "https://api.meethue.com/v2/oauth2/authorize";
    private String hueTokenEndpoint = "https://api.meethue.com/v2/oauth2/token";

    private String hueClientID = "MHXlFaIMPE52gfnlM0nJ1fzYWOLe8fAv";
    private String hueRedirectUri = "com.dabloons.wattsapp:/oauth2redirect";

    private AuthorizationServiceConfiguration serviceConfig;
    private AuthorizationService authService;

    private AuthState authState;

    public OAuth2Manager() {
        serviceConfig =
                new AuthorizationServiceConfiguration(
                        Uri.parse(hueAuthEndpoint), // authorization endpoint
                        Uri.parse(hueTokenEndpoint)); // token endpoint

        authState = new AuthState(serviceConfig);
    }

    public void aquireAuthorizationCode(Context context) {
        AuthorizationRequest.Builder authRequestBuilder =
                new AuthorizationRequest.Builder(
                        serviceConfig, // the authorization service configuration
                        hueClientID, // the client ID, typically pre-registered and static
                        ResponseTypeValues.CODE, // the response_type value: we want a code
                        Uri.parse(hueRedirectUri)); // the redirect URI to which the auth response is sent

        AuthorizationRequest authRequest = authRequestBuilder.build();

        authService = new AuthorizationService(context);
        authService.performAuthorizationRequest(
                authRequest,
                PendingIntent.getActivity(context, 0, new Intent(context, OauthSuccessActivity.class), PendingIntent.FLAG_MUTABLE),
                PendingIntent.getActivity(context, 0, new Intent(context, OauthFailureActivity.class), PendingIntent.FLAG_MUTABLE));
    }

    public AuthState getAuthState() {
        return this.authState;
    }

    public AuthorizationService getAuthService() {
        return this.authService;
    }

    public static OAuth2Manager getInstance() {
        OAuth2Manager result = instance;
        if (result != null) {
            return result;
        }
        synchronized(OAuth2Manager.class) {
            if (instance == null) {
                instance = new OAuth2Manager();
            }
            return instance;
        }
    }
}
