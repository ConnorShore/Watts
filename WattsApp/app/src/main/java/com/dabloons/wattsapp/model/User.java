package com.dabloons.wattsapp.model;

import com.dabloons.wattsapp.model.integration.IntegrationAuth;

import java.util.List;

public class User {

    private String uid;
    private String username;
    private List<IntegrationAuth> integrations;

    public User() { }

    public User(String uid, String username) {
        this.uid = uid;
        this.username = username;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public List<IntegrationAuth> getIntegrations() { return integrations; }

    // --- SETTERS ---
    public void setUsername(String username) { this.username = username; }
    public void setUid(String uid) { this.uid = uid; }
    public void setIntegrations(List<IntegrationAuth> integrations) { this.integrations = integrations; }
}