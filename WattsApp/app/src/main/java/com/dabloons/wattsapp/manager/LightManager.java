package com.dabloons.wattsapp.manager;

import android.util.Log;

import androidx.annotation.NonNull;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.LightState;
import com.dabloons.wattsapp.model.integration.IntegrationAuth;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.model.integration.NanoleafPanelAuthCollection;
import com.dabloons.wattsapp.model.integration.NanoleafPanelIntegrationAuth;
import com.dabloons.wattsapp.repository.LightRepository;
import com.dabloons.wattsapp.service.NanoleafService;
import com.dabloons.wattsapp.service.PhillipsHueService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import util.RepositoryUtil;
import util.UIMessageUtil;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class LightManager {

    private final String LOG_TAG = "LightManager";

    private static volatile LightManager instance;

    private LightRepository lightRepository = LightRepository.getInstance();
    private PhillipsHueService phillipsHueService = PhillipsHueService.getInstance();
    private NanoleafService nanoleafService = NanoleafService.getInstance();

    private final int PHILLIPS_HUE_HUE_MAX = Integer.parseInt(WattsApplication.getResourceString(R.string.phillips_hue_hue_max));
    private final int PHILLIPS_HUE_SATURATION_MAX = Integer.parseInt(WattsApplication.getResourceString(R.string.phillips_hue_saturation_max));
    private final int PHILLIPS_HUE_BRIGHTNESS_MAX = Integer.parseInt(WattsApplication.getResourceString(R.string.phillips_hue_brightness_max));

    private final int NANOLEAF_HUE_MAX = Integer.parseInt(WattsApplication.getResourceString(R.string.nanoleaf_hue_max));
    private final int NANOLEAF_SATURATION_MAX = Integer.parseInt(WattsApplication.getResourceString(R.string.nanoleaf_saturation_max));
    private final int NANOLEAF_BRIGHTNESS_MAX = Integer.parseInt(WattsApplication.getResourceString(R.string.nanoleaf_brightness_max));

    public void turnOnLight(Light light, WattsCallback<Void, Void> callback) {
        LightState state = new LightState(true, light.getLightState().getBrightness(), light.getLightState().getHue(), light.getLightState().getSaturation());
        setLightState(light, state, callback);
    }

    public void turnOffLight(Light light, WattsCallback<Void, Void> callback) {
        LightState state = new LightState(false, light.getLightState().getBrightness(), light.getLightState().getHue(), light.getLightState().getSaturation());
        setLightState(light, state, callback);
    }

    public void deleteLightsForUser(WattsCallback<Void, Void> callback) {
        lightRepository.deleteLightsForUser(callback);
    }

    public void updateMultipleLights(List<Light> lights, WattsCallback<Void, Void> callback) {
        lightRepository.setMultipleLights(lights)
            .addOnCompleteListener(task -> {
                callback.apply(null, new WattsCallbackStatus(true));
            })
            .addOnFailureListener(task -> {
                callback.apply(null, new WattsCallbackStatus(false, task.getMessage()));
            });
    }

    public void setLightState(Light light, LightState state, WattsCallback<Void, Void> callback) {
        IntegrationType type = light.getIntegrationType();
        switch(light.getIntegrationType()) {
            case PHILLIPS_HUE:
                phillipsHueService.setLightState(light, state, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e(LOG_TAG, e.getMessage());
                        callback.apply(null, new WattsCallbackStatus(false, e.getMessage()));
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if(response.isSuccessful())
                            updateLightStateInDatabase(light, state, callback);
                        else
                            callback.apply(null, new WattsCallbackStatus(false, response.message()));
                    }
                });
                break;
            case NANOLEAF:
                nanoleafService.setLightState(light, state, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e(LOG_TAG, e.getMessage());
                        callback.apply(null, new WattsCallbackStatus(false, e.getMessage()));
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if(response.isSuccessful())
                            updateLightStateInDatabase(light, state, callback);
                        else
                            callback.apply(null, new WattsCallbackStatus(false, response.message()));
                    }
                });
                break;
            default:
                Log.w(LOG_TAG, "There is no light manager for integration type " + type);
                break;
        }
    }

    private void updateLightStateInDatabase(Light light, LightState state, WattsCallback<Void, Void> callback) {
        light.setLightState(state);
        lightRepository.updateLight(light).addOnCompleteListener(task -> {
            callback.apply(null, new WattsCallbackStatus(true));
        })
        .addOnFailureListener(task -> {
            callback.apply(null, new WattsCallbackStatus(false, task.getMessage()));
        });
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

    public void syncNanoleafLightsToDatabase(NanoleafPanelAuthCollection collection, WattsCallback<Void, Void> callback) {
        lightRepository.getAllLightsForType(IntegrationType.NANOLEAF, (existingLights, status) -> {
            if(!status.success) {
                Log.e(LOG_TAG, status.message);
                callback.apply(null, new WattsCallbackStatus(false, status.message));
                return null;
            }

            // get nanoleaf panel light state
            getNanoleafPanelLightStates(collection, (states, status1) -> {
                if(!status1.success) {
                    callback.apply(null, new WattsCallbackStatus(false, status1.message));
                    return null;
                }

                List<Light> lights = RepositoryUtil.createNanoleafLightsFromAuthCollection(collection, states);
                updateAndCreateLightsInDatabase(lights, existingLights, callback);
                return null;
            });

            return null;
        });
    }

    private void getNanoleafPanelLightStates(NanoleafPanelAuthCollection collection, WattsCallback<Map<String, LightState>, Void> callback) {
        List<NanoleafPanelIntegrationAuth> panels = collection.getPanelAuths();
        getNanoleafPanelLightState(panels, 0, new HashMap<>(), callback);
    }

    private void getNanoleafPanelLightState(List<NanoleafPanelIntegrationAuth> panels, int index,
                                            Map<String, LightState> states, WattsCallback<Map<String, LightState>, Void> callback) {
        if(index >= panels.size()) {
            callback.apply(states, new WattsCallbackStatus(true));
            return;
        }

        NanoleafPanelIntegrationAuth panel = panels.get(index);
        nanoleafService.getLightState(panel, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                callback.apply(null, new WattsCallbackStatus(false, e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(!response.isSuccessful()) {
                    Log.e(LOG_TAG, response.message());
                    callback.apply(null, new WattsCallbackStatus(false, response.message()));
                    return;
                }

                String responseBody = response.body().string();
                LightState state = getNanoleafPanelLightStateFromResponse(responseBody);
                states.put(panel.getName(), state);
                int nextIndex = index + 1;
                getNanoleafPanelLightState(panels, nextIndex, states, callback);
            }
        });
    }

    private LightState getNanoleafPanelLightStateFromResponse(String response) {
        JsonObject bodyObj = JsonParser.parseString(response).getAsJsonObject();

        boolean on = bodyObj.get("on").getAsJsonObject().get("value").getAsBoolean();
        float hue = bodyObj.get("hue").getAsJsonObject().get("value").getAsFloat();
        float saturation = bodyObj.get("sat").getAsJsonObject().get("value").getAsFloat();
        float brightness = bodyObj.get("brightness").getAsJsonObject().get("value").getAsFloat();

        hue /= NANOLEAF_HUE_MAX;
        saturation /= NANOLEAF_SATURATION_MAX;
        brightness /= NANOLEAF_BRIGHTNESS_MAX;

        LightState state = new LightState(on, brightness, hue, saturation);
        return state;
    }

    public void getLights(WattsCallback<List<Light>, Void> callback)
    {
        lightRepository.getAllLights(callback);
    }

    public void getLightsForIntegration(IntegrationType type, WattsCallback<List<Light>, Void> callback) {
        lightRepository.getAllLightsForType(type, callback);
    }

    public void getLightsForIds(List<String> lightIds, WattsCallback<List<Light>, Void> callback) {
        lightRepository.getLightsForIds(lightIds, callback);
    }

    /*
        HELPERS
     */
    private void syncLightsToDatabase(IntegrationType type, WattsCallback<Void, Void> callback) {
        switch(type) {
            case PHILLIPS_HUE:
                syncPhillipsHueLightsToDatabase(callback);
                break;
            case NANOLEAF:
                syncNanoleafLightsToDatabase(callback);
                break;
            default:
                Log.w(LOG_TAG, "Cannot sync lights of type " + type);
        }
    }

    private void syncPhillipsHueLightsToDatabase(WattsCallback<Void, Void> callback) {
        lightRepository.getAllLightsForType(IntegrationType.PHILLIPS_HUE, (existingLights, status) -> {
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
                        JsonObject responseObject = JsonParser.parseString(responseData).getAsJsonObject();
                        List<Light> lights = getPhillipsHueLightsFromResponse(responseObject);
                        updateAndCreateLightsInDatabase(lights, existingLights, callback);
                    } catch (JSONException e) {
                        callback.apply(null, new WattsCallbackStatus(false, e.getMessage()));
                    }
                }
            });

            return null;
        });
    }

    private void syncNanoleafLightsToDatabase(WattsCallback<Void, Void> callback) {
        // Todo: get current light panel states and sync to database
        UserManager.getInstance().getIntegrationAuthData(IntegrationType.NANOLEAF, (auth, status) -> {
            if(!status.success || auth == null) {
                callback.apply(null, new WattsCallbackStatus(false, status.message));
                return null;
            }

            NanoleafPanelAuthCollection collection = (NanoleafPanelAuthCollection) auth;
            syncNanoleafLightsToDatabase(collection, callback);
            return null;
        });
    }

    private List<Light> getPhillipsHueLightsFromResponse(JsonObject responseObject) throws JSONException {
        String userId = UserManager.getInstance().getCurrentUser().getUid();
        int currentLight = 1;
        List<Light> ret = new ArrayList<>();
        while(true) {
            String integrationId = String.valueOf(currentLight);
            JsonObject nextLight = responseObject.getAsJsonObject(integrationId);
            if(nextLight == null)
                break;

            // TODO: Add more fields of state (i.e. on, color, brightness, etc)
            String name = nextLight.get("name").getAsString();

            JsonObject state = nextLight.get("state").getAsJsonObject();
            boolean reachable = state.get("reachable").getAsBoolean();
            if(!reachable) {
                currentLight++;
                continue;
            }

            boolean on = state.get("on").getAsBoolean();
            float brightness = state.get("bri").getAsFloat();
            Float hue = state.get("hue").getAsFloat();
            Float saturation = state.get("sat").getAsFloat();

            // limit hue and saturation
            hue /= PHILLIPS_HUE_HUE_MAX;
            saturation /= PHILLIPS_HUE_SATURATION_MAX;
            brightness /= PHILLIPS_HUE_BRIGHTNESS_MAX;

            LightState lightState = new LightState(on, brightness, hue, saturation);
            Light light = new Light(userId, name, integrationId, IntegrationType.PHILLIPS_HUE, lightState);
            ret.add(light);
            currentLight++;
        }

        return ret;
    }

    private void updateAndCreateLightsInDatabase(List<Light> lights, List<Light> existingLights, WattsCallback<Void, Void> callback) {
        List<Light> toAddLights = removeDuplicatesAndUpdateExistingLights(existingLights, lights);
        lightRepository.setMultipleLights(toAddLights).addOnCompleteListener(val -> {
            lightRepository.setMultipleLights(existingLights).addOnCompleteListener(val2 -> {
                    callback.apply(null, new WattsCallbackStatus(true));
                })
                .addOnFailureListener(task -> {
                    callback.apply(null, new WattsCallbackStatus(false, task.getMessage()));
                });
            })
            .addOnFailureListener(task -> {
                callback.apply(null, new WattsCallbackStatus(false, task.getMessage()));
            });
    }

    private List<Light> removeDuplicatesAndUpdateExistingLights(List<Light> existingLights, List<Light> lights) {
        List<String> existingIds = new ArrayList<>();
        for(Light l : existingLights) {
            existingIds.add(l.getIntegrationId());
        }

        List<Light> ret = new ArrayList<>();
        for(Light l : lights) {
            if(!existingIds.contains(l.getIntegrationId()))
                ret.add(l);
            else {
                Light el = getExistingLightById(l.getIntegrationId(), existingLights);
                el.setLightState(l.getLightState());
            }
        }

        return ret;
    }

    private Light getExistingLightById(String lightId, List<Light> existingLights) {
        for(Light l : existingLights)
            if(lightId.equals(l.getIntegrationId()))
                return l;
        return null;
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
