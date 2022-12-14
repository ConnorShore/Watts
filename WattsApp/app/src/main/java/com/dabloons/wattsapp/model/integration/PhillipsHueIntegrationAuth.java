package com.dabloons.wattsapp.model.integration;

import java.util.UUID;

public class PhillipsHueIntegrationAuth extends IntegrationAuth {

    private String username;
    private String accessToken;
    private String refreshToken;

    public PhillipsHueIntegrationAuth(String username, String accessToken, String refreshToken) {
        super(UUID.randomUUID().toString(), IntegrationType.PHILLIPS_HUE);
        this.username = username;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public PhillipsHueIntegrationAuth() { super(); }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
