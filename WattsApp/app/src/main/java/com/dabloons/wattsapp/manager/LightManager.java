package com.dabloons.wattsapp.manager;

import android.util.Log;

import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.repository.LightRepository;
import com.dabloons.wattsapp.repository.UserRepository;
import com.dabloons.wattsapp.service.PhillipsHueService;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import okhttp3.Callback;
import util.UIMessageUtil;
import util.WattsCallback;

public class LightManager {

    private final String LOG_TAG = "LightManager";

    private static volatile LightManager instance;

    private LightRepository lightRepository = LightRepository.getInstance();

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

    private void turnOnPhillipsHueLight(Light light) {

    }

    public void syncLights() {
        UserManager.getInstance().getUserIntegrations((integrations, successStatus) -> {
            if(!successStatus.success) {
                Log.e(LOG_TAG, "Failed to get user integration when syncing lights: " + successStatus.message);
                return null;
            }

            for(IntegrationType type : integrations) {
                lightRepository.syncLightsToDatabase(type, (nil, status) -> {
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
