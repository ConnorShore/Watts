package com.dabloons.wattsapp.manager;

import androidx.annotation.NonNull;

import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.LightState;
import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.repository.RoomRepository;
import com.dabloons.wattsapp.repository.UserRepository;
import com.dabloons.wattsapp.service.PhillipsHueService;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class RoomManager
{
    private RoomRepository roomRepository;
    private static volatile RoomManager instance;

    private PhillipsHueService phillipsHueService = PhillipsHueService.getInstance();

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

    public void addLightsToRoom(Room room, List<Light> lights, WattsCallback<Void, Void> callback) {
        if(lights.size() == 0)
        {
            callback.apply(null, new WattsCallbackStatus(true));
            return;
        }
        roomRepository.addLightsToRoom(room, lights).addOnCompleteListener(task -> {
            // Lights have been added to room in DB
            PhillipsHueService.getInstance().createGroupWithLights(room, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.apply(null, new WattsCallbackStatus(false, e.getMessage()));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseData = response.body().string();
                    JsonArray arr = JsonParser.parseString(responseData).getAsJsonArray();
                    JsonObject successObj = arr.get(0).getAsJsonObject();
                    String integrationId = successObj.get("success")
                            .getAsJsonObject().get("id").getAsString();
                    room.setIntegrationId(integrationId);
                    roomRepository.setRoomIntegrationId(room.getUid(), integrationId); // may need to do onSuccessListener
                    callback.apply(null, new WattsCallbackStatus(true));
                }
            });
        })
        .addOnFailureListener(task -> {
            callback.apply(null, new WattsCallbackStatus(false, task.getMessage()));
        });
    }

    public void turnOnRoomLights(Room room, WattsCallback<Void, Void> callback) {
        LightState state = new LightState(true, 1.0f);
        phillipsHueService.setRoomLightsState(room, state, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.apply(null, new WattsCallbackStatus(false, "Failed to turn on hue group lights"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                callback.apply(null, new WattsCallbackStatus(true));
            }
        });
    }

    public void turnOffRoomLights(Room room, WattsCallback<Void, Void> callback) {
        LightState state = new LightState(false, 0.0f);
        phillipsHueService.setRoomLightsState(room, state, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.apply(null, new WattsCallbackStatus(false, "Failed to turn off hue group lights"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                callback.apply(null, new WattsCallbackStatus(true));
            }
        });
    }

    public void deleteRoom(String roomId)
    {
        roomRepository.deleteRoom(roomId);
    }

}
