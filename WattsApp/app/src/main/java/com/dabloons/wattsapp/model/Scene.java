package com.dabloons.wattsapp.model;

import java.util.List;

public abstract class Scene extends IntegrationEntity{

    protected Room room;
    protected List<Light> lights;

    public Scene(String uid, IntegrationType integrationType, Room room, List<Light> lights) {
        super(uid, integrationType);
        this.room = room;
        this.lights = lights;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public List<Light> getLights() {
        return lights;
    }

    public void setLights(List<Light> lights) {
        this.lights = lights;
    }
}
