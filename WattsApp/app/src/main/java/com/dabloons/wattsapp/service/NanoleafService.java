package com.dabloons.wattsapp.service;

import androidx.annotation.NonNull;

import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.Light;
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

    private static volatile NanoleafService instance;

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

    public void turnOnLight(Light light, WattsCallback<Void, Void> callback) {
        //TODO: Need to get baseURL from NanoleafAuth based on light's integrationID
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
