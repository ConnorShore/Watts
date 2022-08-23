package com.dabloons.wattsapp.manager;

import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.repository.RoomRepository;
import com.dabloons.wattsapp.repository.UserRepository;

public class RoomManager
{
    private RoomRepository roomRepository;
    private static volatile RoomManager instance;

    private RoomManager()
    {
        roomRepository = RoomRepository.getInstance();
    }

    public static RoomManager getInstance() {
        RoomManager result = instance;
        if (result != null) {
            return result;
        }
        synchronized(UserRepository.class) {
            if (instance == null) {
                instance = new RoomManager();
            }
            return instance;
        }
    }

    public Room createRoom(String roomName)
    {
        return roomRepository.createRoom(roomName);
    }

    public void deleteRoom(String roomId)
    {
        roomRepository.deleteRoom(roomId);
    }

}
