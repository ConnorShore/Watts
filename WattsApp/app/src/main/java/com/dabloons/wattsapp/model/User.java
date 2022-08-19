package com.dabloons.wattsapp.model;

public class User {

    private String uid;
    private String username;

    public User() { }

    public User(String uid, String username) {
        this.uid = uid;
        this.username = username;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }

    // --- SETTERS ---
    public void setUsername(String username) { this.username = username; }
    public void setUid(String uid) { this.uid = uid; }
}