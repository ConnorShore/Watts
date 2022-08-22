package com.dabloons.wattsapp.model.integration;

public abstract class IntegrationAuth {

    private String uid;
    private IntegrationType integrationType;

    public IntegrationAuth(String uid, IntegrationType integrationType) {
        this.integrationType = integrationType;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public IntegrationType getIntegrationType() {
        return integrationType;
    }

    public void setIntegrationType(IntegrationType integrationType) {
        this.integrationType = integrationType;
    }
}
