package com.dabloons.wattsapp.model;

public class RoomModel {

    private String integrationName;

    public String getIntegrationName() {
        return integrationName;
    }

    public void setIntegrationName(String integrationName) {
        this.integrationName = integrationName;
    }

    public RoomModel(String name)
    {
        this.integrationName = name;
    }
}
