package com.dabloons.wattsapp.manager;

import android.content.Context;
import android.util.Log;

import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.model.integration.IntegrationAuth;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.model.User;
import com.dabloons.wattsapp.model.integration.NanoleafPanelAuthCollection;
import com.dabloons.wattsapp.model.integration.NanoleafPanelIntegrationAuth;
import com.dabloons.wattsapp.model.integration.PhillipsHueIntegrationAuth;
import com.dabloons.wattsapp.repository.LightRepository;
import com.dabloons.wattsapp.repository.UserAuthRepository;
import com.dabloons.wattsapp.repository.UserRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import util.WattsCallback;
import util.WattsCallbackStatus;

public class UserManager {

    private final String LOG_TAG = "UserManager";

    private static volatile UserManager instance;

    private UserRepository userRepository;
    private UserAuthRepository userAuthRepository;

    // Todo: Make every singleton class like this (private constructor, vars initalized in constructor)
    private UserManager() {
        userRepository = UserRepository.getInstance();
        userAuthRepository = UserAuthRepository.getInstance();
    }

    public void createUser(){
        userRepository.createUser();
    }

    public Task<User> getUserData(){
        // Get the user from Firestore and cast it to a User model Object
        return userRepository.getUserData().continueWith(task -> task.getResult().toObject(User.class)) ;
    }

    public void getIntegrationAuthData(IntegrationType type, WattsCallback<IntegrationAuth> callback) {
        switch(type) {
            case PHILLIPS_HUE:
                userAuthRepository.getIntegrationAuth(type)
                        .continueWith(task -> task.getResult().toObject(PhillipsHueIntegrationAuth.class))
                        .addOnCompleteListener(task -> {
                            callback.apply(task.getResult());
                        })
                        .addOnFailureListener(task -> {
                            callback.apply(null, new WattsCallbackStatus(task.getMessage()));
                        });
                break;
            case NANOLEAF:
                userAuthRepository.getIntegrationAuth(type)
                        .continueWith(task -> task.getResult().toObject(NanoleafPanelAuthCollection.class))
                        .addOnCompleteListener(task -> {
                            callback.apply(task.getResult());
                        })
                        .addOnFailureListener(task -> {
                            callback.apply(null, new WattsCallbackStatus(task.getMessage()));
                        });
                break;
            default:
                Log.e(LOG_TAG, "Unknown integration for retrieving auth data");
                break;
        }
    }

    public void addIntegrationAuthData(IntegrationType type, IntegrationAuth authData, WattsCallback<Void> callback) {
        switch(type) {
            case PHILLIPS_HUE:
                PhillipsHueIntegrationAuth phAuthData = (PhillipsHueIntegrationAuth) authData;
                userAuthRepository.addPhillipsHueIntegrationToUser(phAuthData)
                        .addOnCompleteListener(task -> {
                            if(task.isComplete())
                                callback.apply(null);
                            else
                                callback.apply(null, new WattsCallbackStatus("Failed to add integration auth data: " + type));
                        })
                        .addOnFailureListener(task -> {
                            callback.apply(null, new WattsCallbackStatus(task.getMessage()));
                        });
                break;
            case NANOLEAF:
                NanoleafPanelAuthCollection nAuth = (NanoleafPanelAuthCollection) authData;
                userAuthRepository.addNanoleafIntegrationToUser(nAuth)
                        .addOnCompleteListener(task -> {
                            if(task.isComplete())
                                callback.apply(null);
                            else
                                callback.apply(null, new WattsCallbackStatus("Failed to add integration auth data: nanoleaf"));
                        })
                        .addOnFailureListener(task -> {
                            callback.apply(null, new WattsCallbackStatus(task.getMessage()));
                        });
                break;
            default:
                Log.e(LOG_TAG, "No integration exists to add: " + type);
        }
    }

    public void getNanoleafPanelIntegrationAuth(String id, WattsCallback<NanoleafPanelIntegrationAuth> callback) {
        userAuthRepository.getIntegrationAuth(IntegrationType.NANOLEAF)
                .addOnCompleteListener(task -> {
                    NanoleafPanelAuthCollection collection = task.getResult().toObject(NanoleafPanelAuthCollection.class);
                    for(NanoleafPanelIntegrationAuth auth : collection.getPanelAuths()) {
                        if(auth.getUid().equals(id)) {
                            callback.apply(auth);
                            return;
                        }
                    }
                    callback.apply(null, new WattsCallbackStatus("NanoleafPanelIntegrationAuth with id does not exist: " + id));
                })
                .addOnFailureListener(task -> {
                    callback.apply(null, new WattsCallbackStatus(task.getMessage()));
                });
    }

    public void getUserIntegrations(WattsCallback<List<IntegrationType>> callback) {
        userAuthRepository.getUserIntegrations(callback);
    }

    public void deleteUser(Context context, WattsCallback<Void> callback){
        // Delete the user account from the Firestore
        this.deleteUserEntities((var, status) -> {
            if(!status.success) {
                Log.e(LOG_TAG, status.message);
                callback.apply(null, new WattsCallbackStatus(status.message));
                return;
            }

            userAuthRepository.deleteIntegrationsForUser((var1, status1) -> {
                if(!status.success) {
                    Log.e(LOG_TAG, status.message);
                    callback.apply(null, new WattsCallbackStatus(status.message));
                    return;
                }

                userRepository.deleteUserFromFirestore()
                        .addOnCompleteListener(task -> {
                            userRepository.deleteUser(context)
                                    .addOnCompleteListener(task1 -> {
                                        userRepository.signOut(WattsApplication.getAppContext());
                                        callback.apply(null);
                                    })
                                    .addOnFailureListener(task1 -> {
                                        callback.apply(null, new WattsCallbackStatus(task1.getMessage()));
                                    });
                        })
                        .addOnFailureListener(task -> {
                            callback.apply(null, new WattsCallbackStatus(task.getMessage()));
                        });
            });
        });
    }

    public Task<Void> signOut(Context context){
        return userRepository.signOut(context);
    }

    public FirebaseUser getCurrentUser(){
        return userRepository.getCurrentUser();
    }

    public Boolean isCurrentUserLogged(){
        return (this.getCurrentUser() != null);
    }

    public Task<Void> setAuthPropString(String prop, String value, IntegrationType type) {
        return userAuthRepository.updatePropertyString(prop, value, type);
    }

    private void deleteUserEntities(WattsCallback<Void> callback) {
        RoomManager.getInstance().deleteRoomsForUser((var, status) -> {
            if(!status.success) {
                Log.e(LOG_TAG, status.message);
                callback.apply(null, new WattsCallbackStatus(status.message));
                return;
            }

            IntegrationSceneManager.getInstance().deleteUserScenes((var1, status1) -> {
                if(!status1.success) {
                    Log.e(LOG_TAG, status1.message);
                    callback.apply(null, new WattsCallbackStatus(status1.message));
                    return;
                }

                SceneManager.getInstance().deleteUserScenes((var11, status11) -> {
                    if(!status11.success) {
                        Log.e(LOG_TAG, status11.message);
                        callback.apply(null, new WattsCallbackStatus(status11.message));
                        return;
                    }

                    LightManager.getInstance().deleteLightsForUser((var111, status111) -> {
                        if(!status111.success) {
                            Log.e(LOG_TAG, status111.message);
                            callback.apply(null, new WattsCallbackStatus(status111.message));
                        }

                        callback.apply(null);
                    });
                });
            });
        });
    }

    public static UserManager getInstance() {
        UserManager result = instance;
        if (result != null) {
            return result;
        }
        synchronized(UserManager.class) {
            if (instance == null) {
                instance = new UserManager();
            }
            return instance;
        }
    }
}
