package com.dabloons.wattsapp.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.LightState;
import com.dabloons.wattsapp.model.integration.IntegrationAuth;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.model.integration.NanoleafPanelAuthCollection;
import com.dabloons.wattsapp.model.integration.NanoleafPanelIntegrationAuth;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.RequestBody;
import okhttp3.Response;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class NanoleafService extends HttpService {

    private final String LOG_TAG = "NanoleafService";

    private static volatile NanoleafService instance;

    private UserManager userManager = UserManager.getInstance();

    private NanoleafService() { super(); }

    public void addNanoleafUser(NanoleafPanelIntegrationAuth authProps, WattsCallback<String, Void> callback) {
        setBaseUrl(authProps.getBaseUrl());
        RequestBody emptyBody = createEmptyRequestBody();
        makeRequestWithBodyAsync("new", RequestType.POST, emptyBody, getStandardHeaders(), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.apply(null, new WattsCallbackStatus(false, e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code() == 403) {
                    // User hasn't put leafs in pair mode
                    String msg = String.format("Nanoleafs [%s] are not in connect mode", authProps.getName());
                    callback.apply(null, new WattsCallbackStatus(false, msg));
                    return;
                }

                String responseBody = response.body().string();
                JsonObject resObj = JsonParser.parseString(responseBody).getAsJsonObject();
                String authToken = resObj.get("auth_token").getAsString();
                callback.apply(authToken, new WattsCallbackStatus(true));
            }
        });
    }

    public void setLightState(Light light, LightState state, Callback callback) {
        if(light.getIntegrationType() != IntegrationType.NANOLEAF) {
            String msg = "Setting light state, integration mismatch";
            Log.e(LOG_TAG, msg);
            return;
        }

        userManager.getIntegrationAuthData(IntegrationType.NANOLEAF, (auth, status) -> {
            NanoleafPanelAuthCollection collection = (NanoleafPanelAuthCollection) auth;
            NanoleafPanelIntegrationAuth panel = collection.findNanoleafPanelAuthForLight(light);
            setBaseUrl(panel.getBaseUrl());
            String path = String.format("%s/state", panel.getAuthToken());

            JsonObject bodyObj = new JsonObject();
            JsonObject value = new JsonObject();
            value.addProperty("value", state.on);
            bodyObj.add("on", value);
            RequestBody body = createRequestBody(bodyObj);

            makeRequestWithBodyAsync(path, RequestType.PUT, body, getStandardHeaders(), callback);
            return null;
        });
    }

    public void getEffectsForLight(Light light, Callback callback) {
        userManager.getIntegrationAuthData(IntegrationType.NANOLEAF, (auth, status) -> {
            NanoleafPanelAuthCollection collection = (NanoleafPanelAuthCollection) auth;
            NanoleafPanelIntegrationAuth panel = collection.findNanoleafPanelAuthForLight(light);
            setBaseUrl(panel.getBaseUrl());
            String path = String.format("%s/effects/effectsList", panel.getAuthToken());
            makeRequestAsync(path, RequestType.GET, getStandardHeaders(), callback);
            return null;
        });
    }

    private Map<String, String> getStandardHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return headers;
    }

    public void setBaseUrl() {
        baseUrl = "";   // will get once we connect to hue
    }

    public void setBaseUrl(String url) {
        this.baseUrl = url;
    }

    public static NanoleafService getInstance() {
        NanoleafService result = instance;
        if (result != null) {
            return result;
        }
        synchronized(NanoleafService.class) {
            if (instance == null) {
                instance = new NanoleafService();
            }
            return instance;
        }
    }
}
