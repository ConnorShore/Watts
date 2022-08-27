package com.dabloons.wattsapp.manager;

import android.content.Context;
import android.util.Log;

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

    public void getIntegrationAuthData(IntegrationType type, WattsCallback<IntegrationAuth, Void> callback) {
        switch(type) {
            case PHILLIPS_HUE:
                userAuthRepository.getIntegrationAuth(type)
                        .continueWith(task -> task.getResult().toObject(PhillipsHueIntegrationAuth.class))
                        .addOnCompleteListener(task -> {
                            callback.apply(task.getResult(), new WattsCallbackStatus(true));
                        })
                        .addOnFailureListener(task -> {
                            callback.apply(null, new WattsCallbackStatus(false, task.getMessage()));
                        });
                break;
            case NANOLEAF:
                userAuthRepository.getIntegrationAuth(type)
                        .continueWith(task -> task.getResult().toObject(NanoleafPanelAuthCollection.class))
                        .addOnCompleteListener(task -> {
                            callback.apply(task.getResult(), new WattsCallbackStatus(true));
                        })
                        .addOnFailureListener(task -> {
                            callback.apply(null, new WattsCallbackStatus(false, task.getMessage()));
                        });
                break;
        }
    }

    public void addIntegrationAuthData(IntegrationType type, IntegrationAuth authData, WattsCallback<Void, Void> callback) {
        switch(type) {
            case PHILLIPS_HUE:
                PhillipsHueIntegrationAuth phAuthData = (PhillipsHueIntegrationAuth) authData;
                userAuthRepository.addPhillipsHueIntegrationToUser(phAuthData)
                        .addOnCompleteListener(task -> {
                            if(task.isComplete())
                                callback.apply(null, new WattsCallbackStatus(true));
                            else
                                callback.apply(null, new WattsCallbackStatus(false, "Failed to add integration auth data: " + type));
                        })
                        .addOnFailureListener(task -> {
                            callback.apply(null, new WattsCallbackStatus(false, task.getMessage()));
                        });
                break;
            case NANOLEAF:
                NanoleafPanelAuthCollection nAuth = (NanoleafPanelAuthCollection) authData;
                userAuthRepository.addNanoleafIntegrationToUser(nAuth)
                        .addOnCompleteListener(task -> {
                            if(task.isComplete())
                                callback.apply(null, new WattsCallbackStatus(true));
                            else
                                callback.apply(null, new WattsCallbackStatus(false, "Failed to add integration auth data: " + type));
                        })
                        .addOnFailureListener(task -> {
                            callback.apply(null, new WattsCallbackStatus(false, task.getMessage()));
                        });
                break;
            default:
                Log.e(LOG_TAG, "No integration exists to add: " + type);
        }
    }

    public void getUserIntegrations(WattsCallback<List<IntegrationType>, Void> callback) {
        userAuthRepository.getUserIntegrations(callback);
    }

    public void deleteUser(Context context, WattsCallback<Void, Void> callback){
        // Delete the user account from the Firestore
        this.deleteUserEntities((var, status) -> {
            if(!status.success) {
                Log.e(LOG_TAG, status.message);
                callback.apply(null, new WattsCallbackStatus(false, status.message));
                return null;
            }

            userRepository.deleteUserFromFirestore()
                .addOnCompleteListener(task -> {
                    // Once done, delete the user data from Auth
                    userRepository.deleteUser(context)
                        .addOnCompleteListener(task1 -> {
                            callback.apply(null, new WattsCallbackStatus(true));
                        })
                        .addOnFailureListener(task1 -> {
                            callback.apply(null, new WattsCallbackStatus(false, task1.getMessage()));
                        });
            })
            .addOnFailureListener(task -> {
                callback.apply(null, new WattsCallbackStatus(false, task.getMessage()));
            });

            return null;
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

    private void deleteUserEntities(WattsCallback<Void, Void> callback) {
        RoomManager.getInstance().deleteRoomsForUser((var, status) -> {
            if(!status.success) {
                Log.e(LOG_TAG, status.message);
                callback.apply(null, new WattsCallbackStatus(false, status.message));
                return null;
            }

            SceneManager.getInstance().deleteUserScenes((WattsCallback<Void, Void>) (var1, status1) -> {
                if(status1.success) {
                    Log.e(LOG_TAG, status1.message);
                    callback.apply(null, new WattsCallbackStatus(false, status1.message));
                    return null;
                }

                LightManager.getInstance().deleteLightsForUser((WattsCallback<Void, Void>) (var11, status11) -> {
                    callback.apply(null, new WattsCallbackStatus(true));
                    return null;
                });

                return null;
            });

            return null;
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
