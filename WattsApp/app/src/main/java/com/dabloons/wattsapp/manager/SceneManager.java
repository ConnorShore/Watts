package com.dabloons.wattsapp.manager;

import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.model.Scene;
import com.dabloons.wattsapp.model.integration.IntegrationScene;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.repository.SceneRepository;
import com.dabloons.wattsapp.service.NanoleafService;
import com.dabloons.wattsapp.service.PhillipsHueService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import util.PHUtilities;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class SceneManager {

    private final String LOG_TAG = "SceneManager";

    private static volatile  SceneManager instance;

    private final int ANDROID_HUE_MAX = Integer.parseInt(WattsApplication.getResourceString(R.string.color_picker_hue_max));

    private final int PHILLIPS_HUE_HUE_MAX = Integer.parseInt(WattsApplication.getResourceString(R.string.phillips_hue_hue_max));
    private final int PHILLIPS_HUE_SATURATION_MAX = Integer.parseInt(WattsApplication.getResourceString(R.string.phillips_hue_saturation_max));
    private final int PHILLIPS_HUE_BRIGHTNESS_MAX = Integer.parseInt(WattsApplication.getResourceString(R.string.phillips_hue_brightness_max));

    private final int COLOR_TEMPERATURE_ADJUSTMENT = 2000;

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

    public void createScene(String roomID, String sceneName, List<IntegrationScene> sceneList, WattsCallback<Scene> callback)
    {
        this.getSceneColors(sceneList, (sceneColors, status) -> {
            List<Integer> colors = new ArrayList<>(sceneColors);
            sceneRepository.createScene(roomID, sceneName, sceneList, colors, callback);
        });
    }

    public void getAllScenes(String roomID, WattsCallback<List<Scene>> callback)
    {
        sceneRepository.getAllScenes(roomID, callback);
    }

    public void deleteUserScenes(WattsCallback<Void> callback) {
        sceneRepository.deleteScenesForUser(callback);
    }

    public void deleteScene(Scene scene, WattsCallback<Void> callback) {
        sceneRepository.deleteScene(scene)
                .addOnCompleteListener(task -> {
                    callback.apply(null, new WattsCallbackStatus(true));
                })
                .addOnFailureListener(task -> {
                    callback.apply(null, new WattsCallbackStatus(false, task.getMessage()));
                });
    }

    public void activateScene(Scene scene, WattsCallback<Void> callback) {
        scene.setOn(true);
        List<IntegrationScene> scenes = scene.getIntegrationScenes();
        roomManager.getRoomForId(scene.getRoomId(), (room, status) -> {
            activateIntegrationScenes(room, scenes, 0, (var, status1) -> {
                if(!status1.success) {
                    Log.e(LOG_TAG, status1.message);
                    callback.apply(null, new WattsCallbackStatus(status1.message));
                    return;
                }

                sceneRepository.activateScene(scene)
                        .addOnCompleteListener(task -> callback.apply(null))
                        .addOnFailureListener(task -> callback.apply(null, new WattsCallbackStatus(task.getMessage())));
            });
        });
    }

    public void deactivateScene(Scene scene, WattsCallback<Void> callback) {
        scene.setOn(false);
        sceneRepository.deactivateScene(scene)
                .addOnCompleteListener(task -> callback.apply(null))
                .addOnFailureListener(task -> callback.apply(null, new WattsCallbackStatus(task.getMessage())));
    }

    private void activateIntegrationScenes(Room room, List<IntegrationScene> scenes, int index, WattsCallback<Void> callback) {
        if(index >= scenes.size()) {
            callback.apply(null);
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

    private void activatePhillipsHueScene(IntegrationScene scene, Room room, int index, List<IntegrationScene> scenes, WattsCallback<Void> callback) {
        phillipsHueService.activateScene(scene, room, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.apply(null, new WattsCallbackStatus(e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(!response.isSuccessful()) {
                    callback.apply(null, new WattsCallbackStatus(response.message()));
                    return;
                }

                int next = index + 1;
                activateIntegrationScenes(room, scenes, next, callback);
            }
        });
    }

    private void activateNanoleafScene(IntegrationScene scene, Room room, int index, List<IntegrationScene> scenes, WattsCallback<Void> callback) {
        String lightId = scene.getParentLightId();
        userManager.getNanoleafPanelIntegrationAuth(lightId, (panel, status) -> {
            nanoleafService.activateEffectForLight(panel, scene, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.apply(null, new WattsCallbackStatus(e.getMessage()));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.apply(null, new WattsCallbackStatus(response.message()));
                        return;
                    }

                    int next = index + 1;
                    activateIntegrationScenes(room, scenes, next, callback);
                }
            });
        });
    }

    public void getSceneColors(List<IntegrationScene> integrationScenes, WattsCallback<Set<Integer>> callback) {
        Stack<IntegrationScene> scenes = new Stack<>();
        scenes.addAll(integrationScenes);
        getSceneColorsHelper(scenes, new HashSet<>(), callback);
    }

    private void getSceneColorsHelper(Stack<IntegrationScene> scenes, Set<Integer> currentColors, WattsCallback<Set<Integer>> callback) {
        if(scenes.isEmpty()) {
            callback.apply(currentColors);
            return;
        }

        IntegrationScene scene = scenes.pop();
        switch(scene.getIntegrationType()) {
            case PHILLIPS_HUE:
                getColorsForPhillipsHueScene(scene, (var, status) -> {
                    currentColors.addAll(var);
                    getSceneColorsHelper(scenes, currentColors, callback);
                });
                break;
            case NANOLEAF:
                getColorsForNanoleafEffect(scene, (var, status) -> {
                    currentColors.addAll(var);
                    getSceneColorsHelper(scenes, currentColors, callback);
                });
        }
    }

    private void getColorsForPhillipsHueScene(IntegrationScene scene, WattsCallback<List<Integer>> callback) {
        LightManager.getInstance().getLightsForIntegration(IntegrationType.PHILLIPS_HUE, (hueLights, status) -> phillipsHueService.getScene(scene, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                callback.apply(null, new WattsCallbackStatus(e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(!response.isSuccessful()) {
                    Log.e(LOG_TAG, response.message());
                    callback.apply(null, new WattsCallbackStatus(response.message()));
                    return;
                }

                JsonObject responseObj = JsonParser.parseString(response.body().string())
                        .getAsJsonObject();

                JsonArray lights = responseObj.get("lights").getAsJsonArray();
                JsonObject lightStates = responseObj.get("lightstates").getAsJsonObject();
                List<Integer> retColors = new ArrayList<>();
                for(int i = 0; i < lights.size(); i++) {
                    String id = lights.get(i).getAsString();
                    JsonObject obj = lightStates.get(id).getAsJsonObject();

                    Light light = getLightFromIntegrationId(id, hueLights);

                    JsonElement hue = obj.get("hue");
                    JsonElement sat = obj.get("sat");
                    JsonElement ct = obj.get("ct");
                    JsonElement xy = obj.get("xy");

                    int color;
                    if(hue != null && sat != null) {
                        float hueVal = hue.getAsFloat();
                        float satVal = sat.getAsFloat();

                        // limit hue and saturation
                        hueVal /= PHILLIPS_HUE_HUE_MAX;
                        satVal /= PHILLIPS_HUE_SATURATION_MAX;

                        color = Color.HSVToColor(new float[] {hueVal * ANDROID_HUE_MAX, satVal, 1.0f});
                    }
                    else if(ct != null) {
                        float ctVal = ct.getAsFloat();
                        // Convert Mired Colored Temperature to normal temperature
                        int kTemp = (int)(1000000.0f / ctVal);
                        kTemp += COLOR_TEMPERATURE_ADJUSTMENT;  // Adjustment to make color appear more accurate
                        color = PHUtilities.getRGBFromK(kTemp);
                    }
                    else if(xy != null) {
                        JsonArray xyVals = xy.getAsJsonArray();
                        float x = xyVals.get(0).getAsFloat();
                        float y = xyVals.get(1).getAsFloat();
                        color = PHUtilities.colorFromXY(new float[]{x,y}, light.getLightModel());
                    }
                    else {
                        // Can't define the color
                        continue;
                    }

                    retColors.add(color);
                }

                callback.apply(retColors);
            }
        }));
    }

    private Light getLightFromIntegrationId(String id, List<Light> lights) {
        for(Light l : lights)
            if(l.getIntegrationId().equals(id))
                return l;
        return null;
    }

    private void getColorsForNanoleafEffect(IntegrationScene scene, WattsCallback<List<Integer>> callback) {
        String lightId = scene.getParentLightId();
        userManager.getNanoleafPanelIntegrationAuth(lightId, (auth, status) -> nanoleafService.getEffectDetails(auth, scene.getName(), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                callback.apply(null, new WattsCallbackStatus(e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(LOG_TAG, response.message());
                    callback.apply(null, new WattsCallbackStatus(response.message()));
                    return;
                }

                JsonObject responseObj = JsonParser.parseString(response.body().string())
                        .getAsJsonObject();

                JsonArray colorArray = responseObj.getAsJsonArray("palette");
                List<Integer> retColors = new ArrayList<>();
                for(int i = 0; i < colorArray.size(); i++) {
                    JsonObject obj = colorArray.get(i).getAsJsonObject();
                    float hue = obj.get("hue").getAsFloat();
                    float sat = obj.get("saturation").getAsFloat() / 100.0f;
                    float bri = obj.get("brightness").getAsFloat() / 100.0f;

                    int color = Color.HSVToColor(new float[] {hue,sat,bri});
                    retColors.add(color);
                }

                callback.apply(retColors);
            }
        }));
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
