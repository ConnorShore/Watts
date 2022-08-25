package com.dabloons.wattsapp.service;


import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.LightState;
import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.model.integration.PhillipsHueIntegrationAuth;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.RequestBody;

public class PhillipsHueService extends HttpService {

    private final String LOG_TAG = "PhillipsHueService";

    private static volatile PhillipsHueService instance;
    private final UserManager userManager = UserManager.getInstance();

    private final String CONTENT_TYPE = "application/json";

    private PhillipsHueService() {
        super();
    }

    public void linkButton(String accessToken, Callback callback) {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("linkbutton", true);

        RequestBody body = createRequestBody(jsonObj);
        makeRequestWithBodyAsync("0/config", RequestType.PUT, body, getStandardHeaders(accessToken), callback);
    }

    public void getUsername(String accessToken, Callback callback) {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("devicetype", userManager.getCurrentUser().getUid());

        RequestBody body = createRequestBody(jsonObj);
        makeRequestWithBodyAsync("", RequestType.POST, body, getStandardHeaders(accessToken), callback);
    }

    public void getAllLights(Callback callback) {
        userManager.getIntegrationAuthData(IntegrationType.PHILLIPS_HUE).addOnCompleteListener(val -> {
            PhillipsHueIntegrationAuth auth = (PhillipsHueIntegrationAuth)val.getResult();
            String accessToken = auth.getAccessToken();
            String username = auth.getUsername();

            String url = username + "/lights";
            makeRequestAsync(url, RequestType.GET, getStandardHeaders(accessToken), callback);
        });
    }

    public void createGroupWithLights(Room room, Callback callback) {
        userManager.getIntegrationAuthData(IntegrationType.PHILLIPS_HUE).addOnCompleteListener(val -> {
            PhillipsHueIntegrationAuth auth = (PhillipsHueIntegrationAuth)val.getResult();
            String accessToken = auth.getAccessToken();
            String username = auth.getUsername();


            JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty("name", room.getName());
            jsonObj.addProperty("type", "LightGroup");
            JsonArray lightsArr = new JsonArray();
            for(Light light : room.getLights()) {
                lightsArr.add(light.getIntegrationId());
            }
            jsonObj.add("lights", lightsArr);
            RequestBody body = createRequestBody(jsonObj);

            String url = username + "/groups";
            makeRequestWithBodyAsync(url, RequestType.POST, body, getStandardHeaders(accessToken), callback);
        });
    }

    public void turnOnLight(Light light, Callback callback) {
        userManager.getIntegrationAuthData(IntegrationType.PHILLIPS_HUE).addOnSuccessListener(val -> {
            PhillipsHueIntegrationAuth auth = (PhillipsHueIntegrationAuth)val;
            String accessToken = auth.getAccessToken();
            String username = auth.getUsername();

            JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty("on", true);
            RequestBody body = createRequestBody(jsonObj);

            String url = username + "/lights/" + light.getIntegrationId() + "/state";
            makeRequestWithBodyAsync(url, RequestType.PUT, body, getStandardHeaders(accessToken), callback);
        });
    }

    public void setRoomLightsState(Room room, LightState state, Callback callback) {
        // Dont need to make any calls if no lights
        if(room.getLights().size() == 0) {
            try {
                callback.onResponse(null, null);
            } catch(IOException e) {
                callback.onFailure(null, e);
            }
            return;
        }

        userManager.getIntegrationAuthData(IntegrationType.PHILLIPS_HUE).addOnSuccessListener(val -> {
            PhillipsHueIntegrationAuth auth = (PhillipsHueIntegrationAuth)val;
            String accessToken = auth.getAccessToken();
            String username = auth.getUsername();

            int brightness = (int)(state.brightness * 254.0f);

            JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty("on", state.on);
            jsonObj.addProperty("bri", brightness);
            RequestBody body = createRequestBody(jsonObj);

            String url = username + "/groups/" + room.getIntegrationId() + "/action";
            makeRequestWithBodyAsync(url, RequestType.PUT, body, getStandardHeaders(accessToken), callback);
        });
    }

    private Map<String, String> getStandardHeaders(String accessToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("Content-Type", CONTENT_TYPE);
        return headers;
    }

    @Override
    public void setBaseUrl() {
        this.baseUrl = WattsApplication.getResourceString(R.string.hue_api_base_url);
    }

    public static PhillipsHueService getInstance() {
        PhillipsHueService result = instance;
        if (result != null) {
            return result;
        }
        synchronized(PhillipsHueService.class) {
            if (instance == null) {
                instance = new PhillipsHueService();
            }
            return instance;
        }
    }
}
