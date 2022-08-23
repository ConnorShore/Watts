package com.dabloons.wattsapp.model;

import com.dabloons.wattsapp.model.integration.IntegrationType;

public class Light {

    private String uid;
    private String userId;
    private String name;
    private String integrationId;
    private IntegrationType integrationType;

    public Light(String uid, String userId, String name, String integrationId, IntegrationType integrationType) {
        this.uid = uid;
        this.userId = userId;
        this.name = name;
        this.integrationId = integrationId;
        this.integrationType = integrationType;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(String integrationId) {
        this.integrationId = integrationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IntegrationType getIntegrationType() {
        return integrationType;
    }

    public void setIntegrationType(IntegrationType integrationType) {
        this.integrationType = integrationType;
    }
}