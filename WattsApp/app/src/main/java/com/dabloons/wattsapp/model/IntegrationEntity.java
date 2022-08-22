package com.dabloons.wattsapp.model;

import com.dabloons.wattsapp.manager.UserManager;

public class IntegrationEntity {

    private UserManager userManager = UserManager.getInstance();

    protected String uid;
    protected IntegrationType integrationType;
    protected User user;

    public IntegrationEntity(String uid, IntegrationType integrationType) {
        this.uid = uid;
        this.integrationType = integrationType;
        this.userManager.getUserData().addOnSuccessListener(res -> this.user = res);
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
