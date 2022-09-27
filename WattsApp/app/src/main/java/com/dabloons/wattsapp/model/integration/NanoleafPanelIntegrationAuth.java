package com.dabloons.wattsapp.model.integration;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;

import java.util.UUID;

public class NanoleafPanelIntegrationAuth extends IntegrationAuth implements Parcelable {

    private String name;
    private String baseUrl;
    private String authToken;
    private String model;

    @Exclude
    public boolean isSelected;

    public NanoleafPanelIntegrationAuth(String name, String baseUrl, String authToken, String model) {
        super(UUID.randomUUID().toString(), IntegrationType.NANOLEAF);
        this.name = name;
        this.baseUrl = baseUrl;
        this.authToken = authToken;
        this.model = model;
    }

    public NanoleafPanelIntegrationAuth() {
        super();
    }

    protected NanoleafPanelIntegrationAuth(Parcel in) {
        name = in.readString();
        baseUrl = in.readString();
        authToken = in.readString();
        model = in.readString();
        isSelected = in.readByte() != 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Exclude
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(baseUrl);
        parcel.writeString(authToken);
        parcel.writeString(model);
        parcel.writeByte((byte) (isSelected ? 1 : 0));
    }

    public static final Creator<NanoleafPanelIntegrationAuth> CREATOR = new Creator<NanoleafPanelIntegrationAuth>() {
        @Override
        public NanoleafPanelIntegrationAuth createFromParcel(Parcel in) {
            return new NanoleafPanelIntegrationAuth(in);
        }

        @Override
        public NanoleafPanelIntegrationAuth[] newArray(int size) {
            return new NanoleafPanelIntegrationAuth[size];
        }
    };
}
