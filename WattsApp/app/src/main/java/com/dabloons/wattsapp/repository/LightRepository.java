package com.dabloons.wattsapp.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.service.PhillipsHueService;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.dabloons.wattsapp.model.User;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import util.WattsCallback;
import util.WattsCallbackStatus;

public final class LightRepository {

    private final String LOG_TAG = "LightRepository";

    private static volatile LightRepository instance;

    private PhillipsHueService phillipsHueService = PhillipsHueService.getInstance();

    private static final String LIGHT_COLLECTION_NAME = "lights";

    public static LightRepository getInstance() {
        LightRepository result = instance;
        if (result != null) {
            return result;
        }
        synchronized(LightRepository.class) {
            if (instance == null) {
                instance = new LightRepository();
            }
            return instance;
        }
    }

    // Create User in Firestore
    public Task<Void> createLight(String integrationId, IntegrationType type, String name) {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null) return null;

        String userId = user.getUid();

        String uid = UUID.randomUUID().toString();
        Light lightToCreate = new Light(uid, userId, name, integrationId, type);
        return getLightCollection().document(uid).set(lightToCreate);
    }

    public void getAllLights(WattsCallback<List<Light>, Void> callback) {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null) return;

        getLightCollection().get().addOnCompleteListener(task -> {
            if(!task.isComplete())
                Log.e(LOG_TAG, "Failed to get lights collection");

            List<Light> ret = new ArrayList<>();
            for (QueryDocumentSnapshot document : task.getResult()) {
                if(document.get("userId").toString().equals(user.getUid()))
                    ret.add(document.toObject(Light.class));
            }

            callback.apply(ret, new WattsCallbackStatus(true));
        });
    }

    public void syncLightsToDatabase(IntegrationType integration, WattsCallback<Void, Void> callback) {
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

                } catch (JSONException e) {
                    callback.apply(null, new WattsCallbackStatus(false, e.getMessage()));
                }
            }
        });
    }

    // Get the User Collection Reference
    private CollectionReference getLightCollection(){
        return FirebaseFirestore.getInstance().collection(LIGHT_COLLECTION_NAME);
    }
}