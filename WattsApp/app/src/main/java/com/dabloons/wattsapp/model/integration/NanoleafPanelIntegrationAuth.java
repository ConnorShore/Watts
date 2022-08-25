package com.dabloons.wattsapp.model.integration;

import java.util.UUID;

public class NanoleafPanelIntegrationAuth extends IntegrationAuth {

    private String name;
    private String baseUrl;
    private String authToken;

    public NanoleafPanelIntegrationAuth(String name, String baseUrl, String authToken) {
        super(UUID.randomUUID().toString(), IntegrationType.NANOLEAF);
        this.name = name;
        this.baseUrl = baseUrl;
        this.authToken = authToken;
    }

    public NanoleafPanelIntegrationAuth() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
