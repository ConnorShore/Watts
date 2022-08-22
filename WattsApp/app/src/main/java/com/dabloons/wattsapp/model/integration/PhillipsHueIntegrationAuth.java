package com.dabloons.wattsapp.model.integration;

public class PhillipsHueIntegrationAuth extends IntegrationAuth {

    private String userID;
    private String username;
    private String accessToken;
    private String refreshToken;

    public PhillipsHueIntegrationAuth(String uid, String userID, String username, String accessToken, String refreshToken) {
        super(uid, IntegrationType.PHILLIPS_HUE);
        this.userID = userID;
        this.username = username;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

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
