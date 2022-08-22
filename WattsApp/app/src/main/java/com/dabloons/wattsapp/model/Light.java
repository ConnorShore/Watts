package com.dabloons.wattsapp.model;

public abstract class Light extends IntegrationEntity{

    protected Room room;

    public Light(String uid, IntegrationType integrationType, Room room) {
        super(uid, integrationType);
        this.room = room;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
