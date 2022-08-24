package com.dabloons.wattsapp.repository;

import android.util.Log;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.Light;
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

import util.WattsCallback;
import util.WattsCallbackStatus;

public final class LightRepository {

    private final String LOG_TAG = "LightRepository";

    private static volatile LightRepository instance;

    private PhillipsHueService phillipsHueService = PhillipsHueService.getInstance();

    private final String LIGHT_COLLECTION_NAME = WattsApplication.getResourceString(R.string.collection_lights);
    private final String USER_ID_FIELD = WattsApplication.getResourceString(R.string.field_userId);

    // Create User in Firestore
    public Task<Void> createLight(String integrationId, IntegrationType type, String name) {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null) return null;

        String userId = user.getUid();

        String uid = UUID.randomUUID().toString();
        Light lightToCreate = new Light(uid, userId, name, integrationId, type);
        return getLightCollection().document(uid).set(lightToCreate);
    }

    public Task<Void> storeMultipleLights(List<Light> lights) {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null) return null;

        String userId = user.getUid();

        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        for(Light light : lights) {
            DocumentReference ref = getLightCollection().document(light.getUid());
            batch.set(ref, light);
        }
        return batch.commit();
    }

    public void getAllLights(WattsCallback<List<Light>, Void> callback) {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null) return;

        getLightCollection().get().addOnCompleteListener(task -> {
            if(!task.isComplete())
                Log.e(LOG_TAG, "Failed to get lights collection");

            List<Light> ret = new ArrayList<>();
            QuerySnapshot val = task.getResult();
            for (QueryDocumentSnapshot document : task.getResult()) {
                if(document.get(USER_ID_FIELD).toString().equals(user.getUid()))
                    ret.add(document.toObject(Light.class));
            }

            callback.apply(ret, new WattsCallbackStatus(true));
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