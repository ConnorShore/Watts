package com.dabloons.wattsapp.model;

import java.util.ArrayList;
import java.util.List;

public class Room {

    private String uid;
    private String userId;
    private String name;

    private List<Light> lights;

    public Room(String uid, String userId, String name) {
        this.uid = uid;
        this.userId = userId;
        this.name = name;
        this.lights = new ArrayList<>();
    }

    public void addLight(Light light) {
        this.lights.add(light);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
}