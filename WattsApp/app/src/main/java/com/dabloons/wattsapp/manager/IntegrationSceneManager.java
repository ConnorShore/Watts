package com.dabloons.wattsapp.manager;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.integration.IntegrationAuth;
import com.dabloons.wattsapp.model.integration.IntegrationScene;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.model.integration.NanoleafPanelAuthCollection;
import com.dabloons.wattsapp.model.integration.NanoleafPanelIntegrationAuth;
import com.dabloons.wattsapp.repository.IntegrationSceneRepository;
import com.dabloons.wattsapp.service.NanoleafService;
import com.dabloons.wattsapp.service.PhillipsHueService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import util.UIMessageUtil;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class IntegrationSceneManager {

    private final String LOG_TAG = "IntegrationSceneManager";

    private static volatile  IntegrationSceneManager instance;

    private IntegrationSceneRepository integrationSceneRepository;
    private LightManager lightManager;
    private PhillipsHueService phillipsHueService;
    private NanoleafService nanoleafService;

    private UserManager userManager = UserManager.getInstance();

    private IntegrationSceneManager() {
        integrationSceneRepository = IntegrationSceneRepository.getInstance();
        lightManager = LightManager.getInstance();
        phillipsHueService = PhillipsHueService.getInstance();
        nanoleafService = NanoleafService.getInstance();
    }

    public void createIntegrationScene(IntegrationType type, String name, String integrationId,
                            List<String> lightIds, @Nullable String parentLightId, WattsCallback<IntegrationScene> callback)
    {
        integrationSceneRepository.createIntegrationScene(type, name, integrationId, lightIds, parentLightId, callback);
    }

    public void getIntegrationScenes(IntegrationType type, WattsCallback<List<IntegrationScene>> callback)
    {
        integrationSceneRepository.getAllIntegrationScenes(type, callback);
    }

    public void deleteUserScenes(WattsCallback<Void> callback) {
        integrationSceneRepository.deleteIntegrationScenesForUser(callback);
    }


    public void getIntegrationScenesMap(List<IntegrationType> integrations, WattsCallback<Map<IntegrationAuth, List<IntegrationScene>>> callback) {
        Stack<IntegrationType> integrationStack = new Stack<>();
        integrationStack.addAll(integrations);
        buildIntegrationSceneMap(integrationStack, new HashMap<>(), callback);
    }

    public void syncNanoleafEffectsToDatabase(WattsCallback<Void> callback) {
        integrationSceneRepository.getAllIntegrationScenes(IntegrationType.NANOLEAF, (existingScenes, status) -> {
            if(!status.success) {
                Log.e(LOG_TAG, status.message);
                callback.apply(null, new WattsCallbackStatus(status.message));
                return;
            }

            lightManager.getLightsForIntegration(IntegrationType.NANOLEAF, (lights, status12) -> {
                if(!status12.success) {
                    Log.e(LOG_TAG, status.message);
                    callback.apply(null, new WattsCallbackStatus(status.message));
                    return;
                }

                getAndStoreEffectsForEachLight(lights, new ArrayList<>(), 0, (integrationScenes, status1) -> {
                    integrationSceneRepository.storeMultipleIntegrationScenes(removeDuplicateIntegrationScenes(existingScenes, integrationScenes))
                        .addOnCompleteListener(task -> {
                            callback.apply(null);
                        })
                        .addOnFailureListener(task -> {
                            callback.apply(null, new WattsCallbackStatus(task.getMessage()));
                        });
                });
            });
        });
    }

    public void syncIntegrationScenes() {
        UserManager.getInstance().getUserIntegrations((integrations, successStatus) -> {
            if(!successStatus.success) {
                Log.e(LOG_TAG, "Failed to get user integration when syncing lights: " + successStatus.message);
                return;
            }

            for(IntegrationType type : integrations) {
                syncIntegrationScenesToDatabase(type, (nil, status) -> {
                    if(status.success)
                        UIMessageUtil.showShortToastMessage(
                                WattsApplication.getAppContext(),
                                "Successfully synced scene: " + type);
                    else
                        UIMessageUtil.showShortToastMessage(
                                WattsApplication.getAppContext(),
                                "Failed to sync scene: " + type);
                });
            }
        });
    }



    private void buildIntegrationSceneMap(Stack<IntegrationType> integrations,
                                          Map<IntegrationAuth, List<IntegrationScene>> map,
                                          WattsCallback<Map<IntegrationAuth, List<IntegrationScene>>> callback) {
        if(integrations.isEmpty()) {
            callback.apply(map);
            return;
        }

        IntegrationType type = integrations.pop();
        getIntegrationScenes(type, (scenes, status) -> {
            if(!status.success) {
                callback.apply(null, new WattsCallbackStatus(status.message));
                return;
            }

            userManager.getIntegrationAuthData(type, (auth, status1) -> {
                if(!status1.success) {
                    callback.apply(null, new WattsCallbackStatus(status1.message));
                    return;
                }

                switch (type) {
                    case PHILLIPS_HUE:
                        map.put(auth, scenes);
                        break;
                    case NANOLEAF:
                        NanoleafPanelAuthCollection collection = (NanoleafPanelAuthCollection) auth;
                        for(NanoleafPanelIntegrationAuth panel : collection.getPanelAuths()) {
                            map.put(panel, getNanoleafPanelScenes(panel, scenes));
                        }
                        break;
                }

                buildIntegrationSceneMap(integrations, map, callback);
            });
        });
    }

    private List<IntegrationScene> getNanoleafPanelScenes(NanoleafPanelIntegrationAuth panel, List<IntegrationScene> scenes) {
        List<IntegrationScene> ret = new ArrayList<>();
        for(IntegrationScene scene : scenes) {
            if(scene.getParentLightId().equals(panel.getUid()))
                ret.add(scene);
        }
        return ret;
    }

    private void syncIntegrationScenesToDatabase(IntegrationType type, WattsCallback<Void> callback) {
        switch(type) {
            case PHILLIPS_HUE:
                syncPhillipsHueIntegrationSceneToDatabase(callback);
                break;
            case NANOLEAF:
                syncNanoleafEffectsToDatabase(callback);
                break;
            default:
                Log.w(LOG_TAG, "Cannot sync scenes of type " + type);
        }
    }

    private void syncPhillipsHueIntegrationSceneToDatabase(WattsCallback<Void> callback) {
        integrationSceneRepository.getAllIntegrationScenes(IntegrationType.PHILLIPS_HUE, (existingScenes, status) -> {
            if(!status.success) {
                String message = "Failed to get existing scenes when syncing phillips hue scenes";
                Log.e(LOG_TAG, message);
                callback.apply(null, new WattsCallbackStatus(message));
                return;
            }

            phillipsHueService.getAllScenes(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    String message = "Failed to retrieve phillips hue scenes during sync";
                    Log.e(LOG_TAG, message);
                    callback.apply(null, new WattsCallbackStatus(message));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseData = response.body().string();
                    JsonObject responseObject = (JsonObject) JsonParser.parseString(responseData);
                    List<IntegrationScene> integrationScenes = getPhillipsHueScenesFromResponse(responseObject);
                    integrationSceneRepository.storeMultipleIntegrationScenes(removeDuplicateIntegrationScenes(existingScenes, integrationScenes)).addOnCompleteListener(val -> {
                        callback.apply(null);
                    });
                }
            });
        });
    }

    private List<IntegrationScene> getPhillipsHueScenesFromResponse(JsonObject responseObject) {
        String userId = UserManager.getInstance().getCurrentUser().getUid();
        List<IntegrationScene> ret = new ArrayList<>();
        for(String key : responseObject.keySet()) {
            JsonObject obj = responseObject.get(key).getAsJsonObject();
            String integrationId = key;
            String name = obj.get("name").getAsString();
            JsonArray lightIds = obj.get("lights").getAsJsonArray();
            List<String> lightIdsRet = new ArrayList<>();
            Iterator<JsonElement> idIt = lightIds.iterator();
            while(idIt.hasNext()) {
                lightIdsRet.add(idIt.next().getAsString());
            }
            IntegrationScene is = new IntegrationScene(userId, IntegrationType.PHILLIPS_HUE, name, integrationId, lightIdsRet, null);
            ret.add(is);
        }
        return ret;
    }

    private void getAndStoreEffectsForEachLight(List<Light> lights, List<IntegrationScene> scenes, int index, WattsCallback<List<IntegrationScene>> callback) {
        if(index >= lights.size()) {
            callback.apply(scenes);
            return;
        }

        Light light = lights.get(index);
        nanoleafService.getEffectsForLight(light, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.apply(null, new WattsCallbackStatus(e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = response.body().string();
                JsonArray responseArray = JsonParser.parseString(responseData).getAsJsonArray();
                List<IntegrationScene> integrationScenes = getNanoleafScenesFromResponse(light, responseArray);
                scenes.addAll(integrationScenes);
                int newIndex = index + 1;
                getAndStoreEffectsForEachLight(lights, scenes, newIndex, callback);
            }
        });
    }

    private List<IntegrationScene> getNanoleafScenesFromResponse(Light light, JsonArray responseArray) {
        String userId = UserManager.getInstance().getCurrentUser().getUid();
        List<IntegrationScene> ret = new ArrayList<>();
        Iterator<JsonElement> responseIt = responseArray.iterator();
        while(responseIt.hasNext()) {
            String effect = responseIt.next().getAsString();
            List<String> lightIds = new ArrayList<>();
            lightIds.add(light.getIntegrationId());
            IntegrationScene is = new IntegrationScene(userId, IntegrationType.NANOLEAF, effect, effect, lightIds, light.getIntegrationId());
            ret.add(is);
        }
        return ret;
    }

    private List<IntegrationScene> removeDuplicateIntegrationScenes(List<IntegrationScene> existingScenes, List<IntegrationScene> scenes) {
        List<String> existingIds = new ArrayList<>();
        for(IntegrationScene s : existingScenes) {
            existingIds.add(s.getIntegrationId());
        }

        List<IntegrationScene> ret = new ArrayList<>();
        for(IntegrationScene l : scenes) {
            if(!existingIds.contains(l.getIntegrationId()))
                ret.add(l);
        }

        return ret;
    }

    public static IntegrationSceneManager getInstance() {
        IntegrationSceneManager result = instance;
        if (result != null) {
            return result;
        }
        synchronized(IntegrationSceneManager.class) {
            if (instance == null) {
                instance = new IntegrationSceneManager();
            }
            return instance;
        }
    }
}
