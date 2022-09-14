package com.dabloons.wattsapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.google.firebase.firestore.Exclude;
import com.google.gson.Gson;

import java.util.UUID;

public class Light implements Parcelable {

    private String uid;
    private String userId;
    private String name;
    private String integrationId;
    private IntegrationType integrationType;
    private LightState lightState;

    protected Light(Parcel in) {
        uid = in.readString();
        userId = in.readString();
        name = in.readString();
        integrationId = in.readString();
        integrationType = IntegrationType.valueOf(in.readString());
        lightState = in.readParcelable(LightState.class.getClassLoader());
        isSelected = in.readByte() != 0;
    }

    public static final Creator<Light> CREATOR = new Creator<Light>() {
        @Override
        public Light createFromParcel(Parcel in) {
            return new Light(in);
        }

        @Override
        public Light[] newArray(int size) {
            return new Light[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(userId);
        dest.writeString(name);
        dest.writeString(integrationId);
        dest.writeString(integrationType.name());
        dest.writeParcelable(lightState, flags);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }

    @Exclude
    private boolean isSelected;

    public Light(){ }

    public Light(String userId, String name, String integrationId, IntegrationType integrationType, LightState lightState) {
        this.uid = UUID.randomUUID().toString();
        this.userId = userId;
        this.name = name;
        this.integrationId = integrationId;
        this.integrationType = integrationType;
        this.isSelected = false;
        this.lightState = lightState;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(String integrationId) {
        this.integrationId = integrationId;
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

    public IntegrationType getIntegrationType() {
        return integrationType;
    }

    public void setIntegrationType(IntegrationType integrationType) {
        this.integrationType = integrationType;
    }

    @Exclude
    public boolean isSelected() { return isSelected ; }

    public void setSelected(boolean selected) { this.isSelected = selected; }

    public LightState getLightState() {
        return lightState;
    }

    public void setLightState(LightState lightState) {
        this.lightState = lightState;
    }
}
