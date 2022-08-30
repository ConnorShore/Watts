package com.dabloons.wattsapp.manager;

import android.util.Log;

import androidx.annotation.NonNull;

import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.model.Scene;
import com.dabloons.wattsapp.model.integration.IntegrationAuth;
import com.dabloons.wattsapp.model.integration.IntegrationScene;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.model.integration.NanoleafPanelIntegrationAuth;
import com.dabloons.wattsapp.repository.SceneRepository;
import com.dabloons.wattsapp.repository.UserRepository;
import com.dabloons.wattsapp.service.NanoleafService;
import com.dabloons.wattsapp.service.PhillipsHueService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class SceneManager {

    private final String LOG_TAG = "SceneManager";

    private static volatile  SceneManager instance;

    private SceneRepository sceneRepository;
    private PhillipsHueService phillipsHueService;
    private NanoleafService nanoleafService;
    private UserManager userManager;
    private RoomManager roomManager;

    private SceneManager() {
        sceneRepository = SceneRepository.getInstance();
        phillipsHueService = PhillipsHueService.getInstance();
        nanoleafService = NanoleafService.getInstance();
        userManager = UserManager.getInstance();
        roomManager = RoomManager.getInstance();
    }

    public void createScene(String roomID, String sceneName, List<IntegrationScene> sceneList, WattsCallback<Scene, Void> callback)
    {
        sceneRepository.createScene(roomID, sceneName, sceneList, callback);
    }

    public void getAllScenes(String roomID, WattsCallback<List<Scene>, Void> callback)
    {
        sceneRepository.getAllScenes(roomID, callback);
    }

    public void deleteUserScenes(WattsCallback<Void, Void> callback) {
        sceneRepository.deleteScenesForUser(callback);
    }

    public void deleteScene(Scene scene, WattsCallback<Void, Void> callback) {
        sceneRepository.deleteScene(scene)
                .addOnCompleteListener(task -> {
                    callback.apply(null, new WattsCallbackStatus(true));
                })
                .addOnFailureListener(task -> {
                    callback.apply(null, new WattsCallbackStatus(false, task.getMessage()));
                });
    }

    public void activateScene(Scene scene, WattsCallback<Void, Void> callback) {
        List<IntegrationScene> scenes = scene.getIntegrationScenes();
        roomManager.getRoomForId(scene.getRoomId(), (room, status) -> {
            activateIntegrationScenes(room, scenes, 0, (var, status1) -> {
                if(!status1.success) {
                    Log.e(LOG_TAG, status1.message);
                    callback.apply(null, new WattsCallbackStatus(false, status1.message));
                    return null;
                }

                callback.apply(null, new WattsCallbackStatus(true));
                return null;
            });
            return null;
        });
    }

    private void activateIntegrationScenes(Room room, List<IntegrationScene> scenes, int index, WattsCallback<Void, Void> callback) {
        if(index >= scenes.size()) {
            callback.apply(null, new WattsCallbackStatus(true));
            return;
        }

        IntegrationScene scene = scenes.get(index);
        switch(scene.getIntegrationType()) {
            case PHILLIPS_HUE:
                activatePhillipsHueScene(scene, room, index, scenes, callback);
                break;
            case NANOLEAF:
                activateNanoleafScene(scene, room, index, scenes, callback);
                break;
            default:
                Log.e(LOG_TAG, "Cannot activate scene for integration: " + scene.getIntegrationType());
                break;
        }
    }

    private void activatePhillipsHueScene(IntegrationScene scene, Room room, int index, List<IntegrationScene> scenes, WattsCallback<Void, Void> callback) {
        phillipsHueService.activateScene(scene, room, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.apply(null, new WattsCallbackStatus(false, e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(!response.isSuccessful()) {
                    callback.apply(null, new WattsCallbackStatus(false, response.message()));
                    return;
                }

                int next = index + 1;
                activateIntegrationScenes(room, scenes, next, callback);
            }
        });
    }

    private void activateNanoleafScene(IntegrationScene scene, Room room, int index, List<IntegrationScene> scenes, WattsCallback<Void, Void> callback) {
        String lightId = scene.getParentLightId();
        userManager.getNanoleafPanelIntegrationAuth(lightId, (panel, status) -> {
            nanoleafService.activateEffectForLight(panel, scene, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.apply(null, new WattsCallbackStatus(false, e.getMessage()));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.apply(null, new WattsCallbackStatus(false, response.message()));
                        return;
                    }

                    int next = index + 1;
                    activateIntegrationScenes(room, scenes, next, callback);
                }
            });
            return null;
        });
    }

    public static SceneManager getInstance() {
        SceneManager result = instance;
        if (result != null) {
            return result;
        }
        synchronized(SceneManager.class) {
            if (instance == null) {
                instance = new SceneManager();
            }
            return instance;
        }
    }
}
