package com.dabloons.wattsapp.manager.auth;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.dabloons.wattsapp.manager.auth.callback.OnFailureActivity;
import com.dabloons.wattsapp.manager.auth.callback.OnSuccessActivity;
import com.dabloons.wattsapp.model.IntegrationType;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenResponse;

import javax.annotation.Nullable;

public abstract class OAuthManager {

    private AuthState authState;
    private AuthorizationServiceConfiguration serviceConfig;
    private AuthorizationService authService;

    private String authEndpoint;
    private String tokenEndpoint;

    public OAuthManager(String authEndpoint, String tokenEndpoint) {
        this.authEndpoint = authEndpoint;
        this.tokenEndpoint = tokenEndpoint;

        serviceConfig = new AuthorizationServiceConfiguration(
                Uri.parse(authEndpoint),
                Uri.parse(tokenEndpoint));
        authState = new AuthState(serviceConfig);
    }

    public void aquireAuthorizationCode(Context context) {
        OAuthConnectionManager.getInstance().startConnection(getIntegrationType());

        AuthorizationRequest.Builder authRequestBuilder =
                new AuthorizationRequest.Builder(
                        serviceConfig, // the authorization service configuration
                        getClientID(), // the client ID, typically pre-registered and static
                        ResponseTypeValues.CODE, // the response_type value: we want a code
                        getRedirectUri()); // the redirect URI to which the auth response is sent

        AuthorizationRequest authRequest = authRequestBuilder.build();

        authService = new AuthorizationService(context);
        authService.performAuthorizationRequest(
                authRequest,
                PendingIntent.getActivity(context, 0, new Intent(context, OnSuccessActivity.class), PendingIntent.FLAG_MUTABLE),
                PendingIntent.getActivity(context, 0, new Intent(context, OnFailureActivity.class), PendingIntent.FLAG_MUTABLE));
    }

    public abstract void handleAuthResponseSuccess(Intent intent);
    public abstract void handleAuthResponseFailure(Intent intent);

    public abstract String getClientID();
    public abstract String getClientSecret();
    public abstract Uri getRedirectUri();
    public abstract IntegrationType getIntegrationType();

    protected void updateAuthState(@Nullable TokenResponse response, @Nullable AuthorizationException ex) {
        this.authState.update(response, ex);
    }

    protected void updateAuthState(@Nullable AuthorizationResponse response, @Nullable AuthorizationException ex) {
        this.authState.update(response, ex);
    }

    protected void endOauthConnection() {
        OAuthConnectionManager.getInstance().endConnection(getIntegrationType());
    }

    protected AuthorizationService getAuthService() {
        return this.authService;
    }
}
