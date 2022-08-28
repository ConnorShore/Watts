package com.dabloons.wattsapp.repository;

import android.util.Log;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.Scene;
import com.dabloons.wattsapp.model.integration.IntegrationScene;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
                                       List<String> lightIds, WattsCallback<IntegrationScene, Void> callback) {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null)
            return;

        String userId = user.getUid();

        IntegrationScene sceneToCreate = new IntegrationScene(userId, type, name, integrationId, lightIds);
        getIntegrationSceneCollection().document(sceneToCreate.getUid()).set(sceneToCreate)
                .addOnCompleteListener(task -> {
                    if(task.isComplete())
                        callback.apply(sceneToCreate, new WattsCallbackStatus(true));
                    else
                        callback.apply(sceneToCreate, new WattsCallbackStatus(false, "Failed to add integration scene"));
                })
                .addOnFailureListener(task -> {
                    callback.apply(sceneToCreate, new WattsCallbackStatus(false, task.getMessage()));
                });
    }

    public void getAllIntegrationScenes(IntegrationType type, WattsCallback<List<IntegrationScene>, Void> callback)
    {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null) return;

        getIntegrationSceneCollection().get().addOnCompleteListener(task -> {
            if(!task.isComplete())
                Log.e(LOG_TAG, "Failed to get integration secenes collection");

            List<IntegrationScene> ret = new ArrayList<>();
            for(QueryDocumentSnapshot document : task.getResult())
            {
                IntegrationType it = RepositoryUtil.stringToIntegrationType((String)document.get("integrationType"));
                if(it != null)
                {
                    if(it == type)
                        ret.add(document.toObject(IntegrationScene.class));
                }

            }
            callback.apply(ret, new WattsCallbackStatus(true));
        });
    }

    public void deleteIntegrationScenesForUser(WattsCallback<Void, Void> callback) {
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
