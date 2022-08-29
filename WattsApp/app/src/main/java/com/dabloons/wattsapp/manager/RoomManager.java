package com.dabloons.wattsapp.manager;

import androidx.annotation.NonNull;

import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.LightState;
import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.repository.RoomRepository;
import com.dabloons.wattsapp.repository.UserRepository;
import com.dabloons.wattsapp.service.NanoleafService;
import com.dabloons.wattsapp.service.PhillipsHueService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class RoomManager
{
    private static volatile RoomManager instance;

    private RoomRepository roomRepository = RoomRepository.getInstance();
    private PhillipsHueService phillipsHueService = PhillipsHueService.getInstance();
    private NanoleafService nanoleafService = NanoleafService.getInstance();

    private UserManager userManager = UserManager.getInstance();

    private ConcurrentLinkedQueue<IntegrationType> integrationsToResolve;

    private RoomManager()
    {
        integrationsToResolve = new ConcurrentLinkedQueue<>();
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

    public void createRoom(String roomName, WattsCallback<Room, Void> callback)
    {
        roomRepository.createRoom(roomName, callback);
    }

    public void addLightsToRoom(Room room, List<Light> lights, WattsCallback<Void, Void> callback) {
        if(lights.size() == 0)
        {
            callback.apply(null, new WattsCallbackStatus(true));
            return;
        }

        List<IntegrationType> integrationsUsed = integrationsUsedInLights(lights);

        roomRepository.setRoomLights(room, lights).addOnCompleteListener(task -> {
            // Lights have been added to room in DB
            if(integrationsUsed.contains(IntegrationType.PHILLIPS_HUE))
                createPhillipsHueGroup(room, callback);
            else
                callback.apply(null, new WattsCallbackStatus(true));
        })
        .addOnFailureListener(task -> {
            callback.apply(null, new WattsCallbackStatus(false, task.getMessage()));
        });
    }

    public void turnOnRoomLights(Room room, WattsCallback<Void, Void> callback) {
        LightState state = new LightState(true, 1.0f);
        setRoomLightState(room, state, callback);
    }

    public void turnOffRoomLights(Room room, WattsCallback<Void, Void> callback) {
        LightState state = new LightState(false, 0.0f);
        setRoomLightState(room, state, callback);
    }

    public void getRoomForId(String roomId, WattsCallback<Room, Void> callback) {
        roomRepository.getUserDefinedRooms((rooms, status) -> {
            if(!status.success) {
                callback.apply(null, new WattsCallbackStatus(false, status.message));
                return null;
            }
            for(Room r : rooms) {
                if(r.getUid().equals(roomId)) {
                    callback.apply(r, new WattsCallbackStatus(true));
                    return null;
                }
            }
            callback.apply(null, new WattsCallbackStatus(false, "No room with id was found: " + roomId));
            return null;
        });
    }

    public void removeLightFromRoom(Room room, Light light, WattsCallback<Void, Void> callback) {
        List<Light> lights = room.getLights();
        lights.remove(light);
        roomRepository.setRoomLights(room, lights)
                .addOnCompleteListener(task -> {
                    callback.apply(null, new WattsCallbackStatus(true));
                })
                .addOnFailureListener(task -> {
                    callback.apply(null, new WattsCallbackStatus(false, task.getMessage()));
                });
    }

    public void deleteRoom(String roomId, WattsCallback<Void, Void> callback)
    {
        roomRepository.deleteRoom(roomId).addOnCompleteListener(task -> {
                    if(!task.isComplete())
                        callback.apply(null, new WattsCallbackStatus(false, "Failed to delete room"));
                    callback.apply(null, new WattsCallbackStatus(true));
                })
                .addOnFailureListener(task -> {
                    callback.apply(null, new WattsCallbackStatus(false, task.getMessage()));
                });
    }

    public void deleteRoomsForUser(WattsCallback<Void, Void> callback) {
        roomRepository.deleteRoomsForUser(callback);
    }

    /*
     * HELPERS
     */

    private void createPhillipsHueGroup(Room room, WattsCallback<Void, Void> callback) {
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
    }

    private void setRoomLightState(Room room, LightState state, WattsCallback<Void, Void> callback) {
        userManager.getUserIntegrations((integrations, status) -> {
            setIntegrationsToResolve(integrations);
            for(IntegrationType integration : integrations) {
                switch (integration) {
                    case PHILLIPS_HUE:
                        setPhillipsHueRoomLightState(room, state, (var, status1) -> {
                            resolveIntegration(integration, callback);
                            return null;
                        });
                        break;
                    case NANOLEAF:
                        setNanoleafRoomLightState(room, state, (var, status1) -> {
                            resolveIntegration(integration, callback);
                            return null;
                        });
                        break;
                }
            }

            return null;
        });
    }

    private void setPhillipsHueRoomLightState(Room room, LightState state, WattsCallback<Void, Void> callback) {
        phillipsHueService.setRoomLightsState(room, state, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.apply(null, new WattsCallbackStatus(false, "Failed to set hue group light state"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                callback.apply(null, new WattsCallbackStatus(true));
            }
        });
    }

    private void setNanoleafRoomLightState(Room room, LightState state, WattsCallback<Void, Void> callback) {
        List<Light> nanoleafs = room.getLightOfIntegration(IntegrationType.NANOLEAF);
        ConcurrentLinkedQueue<Light> remaining = new ConcurrentLinkedQueue<>(nanoleafs);
        setEachNanoleafLightState(remaining, state, callback);
    }

    private void setEachNanoleafLightState(ConcurrentLinkedQueue<Light> remaining, LightState state, WattsCallback<Void, Void> callback) {
        if(remaining.size() == 0) {
            callback.apply(null, new WattsCallbackStatus(true));
            return;
        }

        Light l = remaining.poll();
        nanoleafService.setLightState(l, state, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.apply(null, new WattsCallbackStatus(false, e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(remaining.size() == 0) {
                    callback.apply(null, new WattsCallbackStatus(true));
                    return;
                }

                setEachNanoleafLightState(new ConcurrentLinkedQueue<>(remaining), state, callback);
            }
        });
    }

    private List<IntegrationType> integrationsUsedInLights(List<Light> lights) {
        List<IntegrationType> ret = new ArrayList<>();
        for(Light l : lights)
            if(!ret.contains(l.getIntegrationType()))
                ret.add(l.getIntegrationType());

        return ret;
    }

    private void setIntegrationsToResolve(List<IntegrationType> integrations) {
        this.integrationsToResolve = new ConcurrentLinkedQueue<>(integrations);
    }

    private void resolveIntegration(IntegrationType integration, WattsCallback<Void, Void> onAllResolvedCallback) {
        if(this.integrationsToResolve.contains(integration)) {
            this.integrationsToResolve.remove(integration);
        }

        if(this.integrationsToResolve.size() == 0)
            onAllResolvedCallback.apply(null, new WattsCallbackStatus(true));
    }

    private void resetIntegrationsToResolve() {
        this.integrationsToResolve.clear();
    }
}
