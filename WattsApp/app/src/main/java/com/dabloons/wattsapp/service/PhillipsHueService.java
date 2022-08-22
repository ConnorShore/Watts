package com.dabloons.wattsapp.service;

import com.dabloons.wattsapp.manager.UserManager;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class PhillipsHueService extends HttpService {

    private static volatile PhillipsHueService instance;

    private static final String BASE_URL = "https://api.meethue.com/route/api/";
    private static final UserManager userManager = UserManager.getInstance();

    public PhillipsHueService() {
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

    private Map<String, String> getStandardHeaders(String accessToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("Content-Type", "application/json");
        return headers;
    }

    @Override
    public void setBaseUrl() {
        this.baseUrl = BASE_URL;
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
