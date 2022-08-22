package com.dabloons.wattsapp.model;

import java.util.List;

public abstract class IntegrationBase extends IntegrationEntity {

    public IntegrationBase(String uid, IntegrationType integrationType) {
        super(uid, integrationType);
    }

    public abstract List<Light> getAllLights();
    public abstract List<Scene> getAllScenes();
    public abstract List<Room> getAllRooms();
}
