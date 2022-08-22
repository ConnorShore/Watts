package com.dabloons.wattsapp.model;

import java.util.List;

public abstract class Room extends IntegrationEntity {

    protected List<Light> lights;
    protected List<Scene> scenes;

    public Room(String uid, IntegrationType integrationType, List<Light> lights, List<Scene> scenes) {
        super(uid, integrationType);
        this.lights = lights;
        this.scenes = scenes;
    }

    public List<Light> getLights() {
        return lights;
    }

    public void setLights(List<Light> lights) {
        this.lights = lights;
    }

    public List<Scene> getScenes() {
        return scenes;
    }

    public void setScenes(List<Scene> scenes) {
        this.scenes = scenes;
    }
}
