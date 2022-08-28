package com.dabloons.wattsapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.dabloons.wattsapp.model.integration.IntegrationScene;

import java.util.List;
import java.util.UUID;

public class Scene implements Parcelable {

    private String uid;
    private String userId;
    private String roomId;
    private String name;
    private List<IntegrationScene> sceneList;


    public Scene() {
    }

    public Scene(String userId, String roomId, String sceneName, List<IntegrationScene> sceneList) {
        this.uid = UUID.randomUUID().toString();
        this.userId = userId;
        this.roomId = roomId;
        this.name = sceneName;
        this.sceneList = sceneList;
    }


    protected Scene(Parcel in) {
        uid = in.readString();
        userId = in.readString();
        roomId = in.readString();
        name = in.readString();
        sceneList = in.createTypedArrayList(IntegrationScene.CREATOR);
    }

    public static final Creator<Scene> CREATOR = new Creator<Scene>() {
        @Override
        public Scene createFromParcel(Parcel in) {
            return new Scene(in);
        }

        @Override
        public Scene[] newArray(int size) {
            return new Scene[size];
        }
    };

    public String getUid() { return  this.uid; }
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
    public List<IntegrationScene> getSceneList() {
        return sceneList;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setSceneList(List<IntegrationScene> sceneList) {
        this.sceneList = sceneList;
    }

    public void addSceneToList(IntegrationScene scene) { this.sceneList.add(scene); }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(uid);
        dest.writeString(userId);
        dest.writeString(roomId);
        dest.writeString(name);
        dest.writeTypedList(sceneList);
    }


}
