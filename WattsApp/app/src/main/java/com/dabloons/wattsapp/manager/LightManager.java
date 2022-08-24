package com.dabloons.wattsapp.manager;

import android.util.Log;

import androidx.annotation.NonNull;

import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.repository.LightRepository;
import com.dabloons.wattsapp.service.PhillipsHueService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import util.UIMessageUtil;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class LightManager {

    private final String LOG_TAG = "LightManager";

    private static volatile LightManager instance;

    private LightRepository lightRepository = LightRepository.getInstance();
    private PhillipsHueService phillipsHueService = PhillipsHueService.getInstance();

    public void turnOnLight(Light light, Callback callback) {
        IntegrationType type = light.getIntegrationType();
        switch(light.getIntegrationType()) {
            case PHILLIPS_HUE:
                PhillipsHueService.getInstance().turnOnLight(light, callback);
                break;
            default:
                Log.w(LOG_TAG, "There is no light manager for integration type " + type);
                break;
        }
    }

    public void syncLights() {
        UserManager.getInstance().getUserIntegrations((integrations, successStatus) -> {
            if(!successStatus.success) {
                Log.e(LOG_TAG, "Failed to get user integration when syncing lights: " + successStatus.message);
                return null;
            }

            for(IntegrationType type : integrations) {
                syncLightsToDatabase(type, (nil, status) -> {
                    if(status.success)
                        UIMessageUtil.showShortToastMessage(
                                WattsApplication.getAppContext(),
                                "Successfully synced lights: " + type);
                    else
                        UIMessageUtil.showShortToastMessage(
                                WattsApplication.getAppContext(),
                                "Failed to sync lights: " + type);

                    return null;
                });
            }

            return null;
        });
    }

    public void getLights(WattsCallback<List<Light>, Void> callback)
    {
        LightRepository.getInstance().getAllLights(callback);
    }

    /*
        HELPERS
     */
    private void syncLightsToDatabase(IntegrationType integration, WattsCallback<Void, Void> callback) {
        switch(integration) {
            case PHILLIPS_HUE:
                syncPhillipsHueLightsToDatabase(callback);
                break;
            default:
                Log.e(LOG_TAG, "Cannot sync lights to db from " + integration);
                break;
        }
    }

    private void syncPhillipsHueLightsToDatabase(WattsCallback<Void, Void> callback) {
        lightRepository.getAllLights((existingLights, status) -> {
            if(!status.success) {
                String message = "Failed to get existing lights when syncing phillips hue lights";
                Log.e(LOG_TAG, message);
                callback.apply(null, new WattsCallbackStatus(false, message));
                return null;
            }

            phillipsHueService.getAllLights(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    String message = "Failed to retrieve phillips hue lights during sync";
                    Log.e(LOG_TAG, message);
                    callback.apply(null, new WattsCallbackStatus(false, message));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseData = response.body().string();
                    try {
                        JSONObject responseObject = new JSONObject(responseData);
                        List<Light> lights = getPhillipsHueLightsFromResponse(responseObject);
                        lightRepository.storeMultipleLights(removeDuplicateLights(existingLights, lights)).addOnCompleteListener(val -> {
                            callback.apply(null, new WattsCallbackStatus(true));
                        });
                    } catch (JSONException e) {
                        callback.apply(null, new WattsCallbackStatus(false, e.getMessage()));
                    }
                }
            });

            return null;
        });
    }

    private List<Light> getPhillipsHueLightsFromResponse(JSONObject responseObject) throws JSONException {
        String userId = UserManager.getInstance().getCurrentUser().getUid();
        int currentLight = 1;
        List<Light> ret = new ArrayList<>();
        while(true) {
            String integrationId = String.valueOf(currentLight);
            JSONObject nextLight = null;
            try {
                nextLight = responseObject.getJSONObject(integrationId);
            } catch (JSONException e) {
                // No more lights
                break;
            }

            // TODO: Add more fields of state (i.e. on, color, brightness, etc)
            String name = nextLight.getString("name");

            Light light = new Light(UUID.randomUUID().toString(), userId, name, integrationId, IntegrationType.PHILLIPS_HUE);
            ret.add(light);
            currentLight++;
        }

        return ret;
    }

    private List<Light> removeDuplicateLights(List<Light> existingLights, List<Light> lights) {
        List<String> existingIds = new ArrayList<>();
        for(Light l : existingLights) {
            existingIds.add(l.getIntegrationId());
        }

        List<Light> ret = new ArrayList<>();
        for(Light l : lights) {
            if(!existingIds.contains(l.getIntegrationId()))
                ret.add(l);
        }

        return ret;
    }

    public static LightManager getInstance() {
        LightManager result = instance;
        if (result != null) {
            return result;
        }
        synchronized(LightManager.class) {
            if (instance == null) {
                instance = new LightManager();
            }
            return instance;
        }
    }
}
