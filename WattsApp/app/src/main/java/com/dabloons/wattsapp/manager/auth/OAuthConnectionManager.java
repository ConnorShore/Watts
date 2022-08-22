package com.dabloons.wattsapp.manager.auth;

import com.dabloons.wattsapp.model.integration.IntegrationType;

import java.util.HashMap;
import java.util.Map;

public class OAuthConnectionManager {

    private final String LOG_TAG = "OAuthConnectionManager";

    private static volatile OAuthConnectionManager instance;

    private Map<IntegrationType, Boolean> oAuthConnectionMap;

    public OAuthConnectionManager() {
        this.oAuthConnectionMap = new HashMap<>();
    }

    public void startConnection(IntegrationType type) {
        this.oAuthConnectionMap.put(type, true);
    }

    public void endConnection(IntegrationType type) {
        this.oAuthConnectionMap.put(type, false);
    }

    public IntegrationType getCurrentConnection() {
        for(IntegrationType type : this.oAuthConnectionMap.keySet()) {
            if(this.oAuthConnectionMap.get(type))
                return type;
        }

        return IntegrationType.NONE;
    }

    public static OAuthConnectionManager getInstance() {
        OAuthConnectionManager result = instance;
        if (result != null) {
            return result;
        }
        synchronized(OAuthConnectionManager.class) {
            if (instance == null) {
                instance = new OAuthConnectionManager();
            }
            return instance;
        }
    }
}
