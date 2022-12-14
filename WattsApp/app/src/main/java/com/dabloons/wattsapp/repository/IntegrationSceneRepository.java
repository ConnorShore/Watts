package com.dabloons.wattsapp.repository;

import android.util.Log;

import androidx.annotation.Nullable;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.integration.IntegrationScene;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

import util.FirestoreUtil;
import util.RepositoryUtil;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class IntegrationSceneRepository {

    private final String LOG_TAG = "IntegrationSceneRepository";

    private static volatile IntegrationSceneRepository instance;

    private final String INTEGRATION_SCENES_COLLECTION_NAME = WattsApplication.getResourceString(R.string.collection_integration_scenes);


    public void createIntegrationScene(IntegrationType type, String name, String integrationId,
                                       List<String> lightIds, @Nullable String parentLightId, WattsCallback<IntegrationScene> callback) {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null)
            return;

        String userId = user.getUid();

        IntegrationScene sceneToCreate = new IntegrationScene(userId, type, name, integrationId, lightIds, parentLightId);
        getIntegrationSceneCollection().document(sceneToCreate.getUid()).set(sceneToCreate)
                .addOnCompleteListener(task -> {
                    if(task.isComplete())
                        callback.apply(sceneToCreate);
                    else
                        callback.apply(sceneToCreate, new WattsCallbackStatus("Failed to add integration scene"));
                })
                .addOnFailureListener(task -> {
                    callback.apply(sceneToCreate, new WattsCallbackStatus(task.getMessage()));
                });
    }

    public Task<Void> storeMultipleIntegrationScenes(List<IntegrationScene> integrationScenes) {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null) return null;

        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        for(IntegrationScene scene : integrationScenes) {
            DocumentReference ref = getIntegrationSceneCollection().document(scene.getUid());
            batch.set(ref, scene);
        }
        return batch.commit();
    }

    public void getAllIntegrationScenes(IntegrationType type, WattsCallback<List<IntegrationScene>> callback)
    {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null) return;

        getIntegrationSceneCollection().get().addOnCompleteListener(task -> {
            if(!task.isComplete()) {
                String message = "Failed to get integration secenes collection";
                Log.e(LOG_TAG, message);
                callback.apply(null, new WattsCallbackStatus(message));
                return;
            }

            List<IntegrationScene> ret = new ArrayList<>();
            for(QueryDocumentSnapshot document : task.getResult()) {
                IntegrationType it = RepositoryUtil.stringToIntegrationType((String)document.get("integrationType"));
                if(it != null && it == type)
                    ret.add(document.toObject(IntegrationScene.class));

            }
            callback.apply(ret);
        });
    }

    public void deleteIntegrationScenesForUser(WattsCallback<Void> callback) {
        FirestoreUtil.deleteDocumentsForUser(getIntegrationSceneCollection(), callback);
    }

    // Get the User Collection Reference
    private CollectionReference getIntegrationSceneCollection(){
        return FirebaseFirestore.getInstance().collection(INTEGRATION_SCENES_COLLECTION_NAME);
    }



    public static IntegrationSceneRepository getInstance() {
        IntegrationSceneRepository result = instance;
        if (result != null) {
            return result;
        }
        synchronized(IntegrationSceneRepository.class) {
            if (instance == null) {
                instance = new IntegrationSceneRepository();
            }
            return instance;
        }
    }
}
