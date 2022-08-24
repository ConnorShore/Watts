package com.dabloons.wattsapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Room implements Parcelable {

    private String uid;
    private String userId;
    private String integrationId;
    private String name;
    private List<Light> lights;

    public Room() { }

    public Room(String uid, String userId, String name) {
        this.uid = uid;
        this.userId = userId;
        this.name = name;
        this.lights = new ArrayList<>();
    }

    protected Room(Parcel in) {
        uid = in.readString();
        userId = in.readString();
        integrationId = in.readString();
        name = in.readString();
        lights = new ArrayList<>();
        in.readParcelableList(lights, Light.class.getClassLoader());
    }

    public static final Creator<Room> CREATOR = new Creator<Room>() {
        @Override
        public Room createFromParcel(Parcel in) {
            return new Room(in);
        }

        @Override
        public Room[] newArray(int size) {
            return new Room[size];
        }
    };

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

    public List<Light> getLights() { return lights; }

    public void setLights(List<Light> lights) { this.lights = lights; }

    public String getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(String integrationId) {
        this.integrationId = integrationId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(this.uid);
        dest.writeString(this.userId);
        dest.writeString(this.integrationId);
        dest.writeString(this.name);
        dest.writeList(this.lights);

    }
}
