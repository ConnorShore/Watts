package com.dabloons.wattsapp.repository;

import android.util.Log;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.LightState;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.service.PhillipsHueService;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import util.FirestoreUtil;
import util.RepositoryUtil;
import util.WattsCallback;
import util.WattsCallbackStatus;

public final class LightRepository {

    private final String LOG_TAG = "LightRepository";

    private static volatile LightRepository instance;

    private final String LIGHT_COLLECTION_NAME = WattsApplication.getResourceString(R.string.collection_lights);
    private final String USER_ID_FIELD = WattsApplication.getResourceString(R.string.field_userId);
    private final String LIGHT_ID = WattsApplication.getResourceString(R.string.field_uid);
    private final String INTEGRATION_TYPE_FIELD = WattsApplication.getResourceString(R.string.field_integration_type);

    // Create User in Firestore
    public Task<Void> createLight(String integrationId, IntegrationType type, String name, LightState lightState) {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null) return null;

        String userId = user.getUid();

        Light lightToCreate = new Light(userId, name, integrationId, type, lightState);
        return getLightCollection().document(lightToCreate.getUid()).set(lightToCreate);
    }

    public Task<Void> setMultipleLights(List<Light> lights) {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null) return null;

        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        for(Light light : lights) {
            DocumentReference ref = getLightCollection().document(light.getUid());
            batch.set(ref, light);
        }
        return batch.commit();
    }

    public Task<Void> updateLight(Light light) {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null) return null;

        return getLightCollection().document(light.getUid()).set(light);
    }

    public void getAllLightsForType(IntegrationType type, WattsCallback<List<Light>> callback) {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null) return;

        getLightCollection().get().addOnCompleteListener(task -> {
            if(!task.isComplete()) {
                String message = "Failed to get lights collection";
                Log.e(LOG_TAG, message);
            }

            List<Light> ret = new ArrayList<>();
            for (QueryDocumentSnapshot document : task.getResult()) {
                boolean userEqual = document.get(USER_ID_FIELD).toString().equals(user.getUid());
                boolean typeEqual = type == IntegrationType.NONE
                    || RepositoryUtil.stringToIntegrationType(document.get(INTEGRATION_TYPE_FIELD).toString()) == type;

                if(userEqual && typeEqual)
                    ret.add(document.toObject(Light.class));
            }

            callback.apply(ret);
        });
    }

    public void getAllLights(WattsCallback<List<Light>> callback) {
        getAllLightsForType(IntegrationType.NONE, callback);
    }

    public void deleteLightsForUser(WattsCallback<Void> callback) {
        FirestoreUtil.deleteDocumentsForUser(getLightCollection(), callback);
    }

    public void getLightsForIds(List<String> lightIds, WattsCallback<List<Light>> callback) {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null) return;

        getLightCollection().get().addOnCompleteListener(task -> {
            if(!task.isComplete()) {
                String message = "Failed to get lights collection";
                Log.e(LOG_TAG, message);
            }

            List<Light> ret = new ArrayList<>();
            for (QueryDocumentSnapshot document : task.getResult()) {
                boolean containsLight = lightIds.contains(document.get(LIGHT_ID).toString());
                if(containsLight)
                    ret.add(document.toObject(Light.class));
            }

            callback.apply(ret);
        });
    }

    // Get the User Collection Reference
    private CollectionReference getLightCollection(){
        return FirebaseFirestore.getInstance().collection(LIGHT_COLLECTION_NAME);
    }

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
}