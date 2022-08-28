package com.dabloons.wattsapp.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.RoomManager;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.Scene;
import com.dabloons.wattsapp.model.integration.IntegrationScene;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import util.FirestoreUtil;
import util.WattsCallback;
import util.WattsCallbackStatus;

public final class SceneRepository
{
    private final String LOG_TAG = "SceneRepository";

    private static volatile SceneRepository instance;

    private final String SCENES_COLLECTION_NAME = WattsApplication.getResourceString(R.string.collection_scenes);
    private final String USER_ID_FIELD = WattsApplication.getResourceString(R.string.field_userId);
    private final String SCENES_FIELD_TYPE = WattsApplication.getResourceString(R.string.field_scenes);


    public static SceneRepository getInstance() {
        SceneRepository result = instance;
        if (result != null) {
            return result;
        }
        synchronized(UserRepository.class) {
            if (instance == null) {
                instance = new SceneRepository();
            }
            return instance;
        }
    }

    public void createScene(String roomID, String sceneName, List<IntegrationScene> sceneList, WattsCallback<Scene, Void> callback) {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null)
            return;

        String userId = user.getUid();

        Scene sceneToCreate = new Scene(userId, roomID, sceneName, sceneList);
        getSceneCollection().document(sceneToCreate.getUid()).set(sceneToCreate)
            .addOnCompleteListener(task -> {
                if(task.isComplete())
                    callback.apply(sceneToCreate, new WattsCallbackStatus(true));
                else
                    callback.apply(sceneToCreate, new WattsCallbackStatus(false, "Failed to add scene"));
            })
            .addOnFailureListener(task -> {
                callback.apply(sceneToCreate, new WattsCallbackStatus(false, task.getMessage()));
            });
    }

    public void getAllScenes(String roomID, WattsCallback<List<Scene>, Void> callback)
    {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null) return;

        getSceneCollection().get().addOnCompleteListener(task -> {
           if(!task.isComplete())
               Log.e(LOG_TAG, "Failed to get secenes collection");

           List<Scene> ret = new ArrayList<>();
           for(QueryDocumentSnapshot document : task.getResult())
           {
               Object query = document.get("roomId");
               if(query != null)
               {
                   boolean isInRoom = query.toString().equals(roomID);
                   if(isInRoom)
                       ret.add(document.toObject(Scene.class));
               }

           }
            callback.apply(ret, new WattsCallbackStatus(true));
        });
    }

    public void deleteScenesForUser(WattsCallback<Void, Void> callback) {
        FirestoreUtil.deleteDocumentsForUser(getSceneCollection(), callback);
    }

    // Get the User Collection Reference
    private CollectionReference getSceneCollection(){
        return FirebaseFirestore.getInstance().collection(SCENES_COLLECTION_NAME);
    }
}
