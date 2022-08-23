package com.dabloons.wattsapp.model;

public class Room {

    private String uid;
    private String userId;
    private String name;

    public Room(String uid, String userId, String name) {
        this.uid = uid;
        this.userId = userId;
        this.name = name;
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
