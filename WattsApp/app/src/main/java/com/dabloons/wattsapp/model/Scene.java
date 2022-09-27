package com.dabloons.wattsapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.dabloons.wattsapp.model.integration.IntegrationScene;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Scene implements Parcelable {

    private String uid;
    private String userId;
    private String roomId;
    private String name;
    private boolean on;
    private List<IntegrationScene> integrationScenes;
    private List<Integer> sceneColors;

    public Scene() {
    }

    public Scene(String userId, String roomId, String sceneName, boolean on,
                 List<IntegrationScene> integrationScenes, List<Integer> sceneColors) {
        this.uid = UUID.randomUUID().toString();
        this.userId = userId;
        this.roomId = roomId;
        this.name = sceneName;
        this.on = on;
        this.integrationScenes = integrationScenes;
        this.sceneColors = sceneColors;
    }

    protected Scene(Parcel in) {
        uid = in.readString();
        userId = in.readString();
        roomId = in.readString();
        name = in.readString();
        on = in.readBoolean();
        integrationScenes = in.createTypedArrayList(IntegrationScene.CREATOR);
        sceneColors = Arrays.stream(in.createIntArray()).boxed().collect(Collectors.toList());
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
    public List<IntegrationScene> getIntegrationScenes() {
        return integrationScenes;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setIntegrationScenes(List<IntegrationScene> integrationScenes) {
        this.integrationScenes = integrationScenes;
    }

    public List<Integer> getSceneColors() {
        return sceneColors;
    }

    public void setSceneColors(List<Integer> sceneColors) {
        this.sceneColors = sceneColors;
    }

    @Exclude
    public int[] getSceneColorsArray() {
        return sceneColors.stream().mapToInt(Integer::intValue).toArray();
    }

    public void addIntegrationSceneToList(IntegrationScene scene) { this.integrationScenes.add(scene); }

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
        dest.writeBoolean(on);
        dest.writeTypedList(integrationScenes);
        int[] toWrite = sceneColors.stream().mapToInt(Integer::intValue).toArray();
        dest.writeIntArray(toWrite);
    }
}
