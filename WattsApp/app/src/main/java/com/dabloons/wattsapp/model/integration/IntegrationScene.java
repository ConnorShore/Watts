package com.dabloons.wattsapp.model.integration;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.UUID;

public class IntegrationScene extends IntegrationAuth implements Parcelable {

    private String userId;
    private String name;
    private String integrationId;
    private List<String> lightIds;

    public IntegrationScene(String userId, IntegrationType type, String name, String integrationId, List<String> lightIds) {
        super(UUID.randomUUID().toString(), type);
        this.userId = userId;
        this.name = name;
        this.integrationId = integrationId;
        this.lightIds = lightIds;
    }

    public IntegrationScene() {
        super();
    }

    protected IntegrationScene(Parcel in) {
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

    public String getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(String integrationId) {
        this.integrationId = integrationId;
    }

    public List<String> getLightIds() {
        return lightIds;
    }

    public void setLightIds(List<String> lightIds) {
        this.lightIds = lightIds;
    }

    public static final Creator<IntegrationScene> CREATOR = new Creator<IntegrationScene>() {
        @Override
        public IntegrationScene createFromParcel(Parcel in) {
            return new IntegrationScene(in);
        }

        @Override
        public IntegrationScene[] newArray(int size) {
            return new IntegrationScene[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
