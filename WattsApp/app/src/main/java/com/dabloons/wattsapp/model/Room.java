package com.dabloons.wattsapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.dabloons.wattsapp.model.integration.IntegrationType;

import java.util.ArrayList;
import java.util.List;

public class Room implements Parcelable {

    private String uid;
    private String userId;
    private String integrationId;
    private String name;
    private List<String> lightIds;
    private List<Scene> scenes;

    public Room() { }

    public Room(String uid, String userId, String name) {
        this.uid = uid;
        this.userId = userId;
        this.name = name;
        this.lightIds = new ArrayList<>();
    }

    public Room(String uid, String userId, String name, List<String> lightIds) {
        this.uid = uid;
        this.userId = userId;
        this.name = name;
        this.lightIds = lightIds;
    }

    protected Room(Parcel in) {
        uid = in.readString();
        userId = in.readString();
        integrationId = in.readString();
        name = in.readString();
        lightIds = in.createStringArrayList();
        scenes = in.createTypedArrayList(Scene.CREATOR);
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

    public List<String> getLightIds() { return lightIds; }

    public void setLightIds(List<String> lightIds) { this.lightIds = lightIds; }

    public void appendLightId(String id) {this.lightIds.add(id);}

    public String getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(String integrationId) {
        this.integrationId = integrationId;
    }

    public List<Scene> getScenes() {
        return scenes;
    }

    public void setScenes(List<Scene> scenes) {
        this.scenes = scenes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(userId);
        dest.writeString(integrationId);
        dest.writeString(name);
        dest.writeStringList(lightIds);
        dest.writeTypedList(scenes);
    }
}
