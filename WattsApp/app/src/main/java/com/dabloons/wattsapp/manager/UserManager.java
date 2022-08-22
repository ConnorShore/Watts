package com.dabloons.wattsapp.manager;

import android.content.Context;

import com.dabloons.wattsapp.model.integration.IntegrationAuth;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.model.User;
import com.dabloons.wattsapp.model.integration.PhillipsHueIntegrationAuth;
import com.dabloons.wattsapp.repository.UserAuthRepository;
import com.dabloons.wattsapp.repository.UserRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

public class UserManager {

    private final String LOG_TAG = "UserManager";

    private static volatile UserManager instance;

    private UserRepository userRepository;
    private UserAuthRepository userAuthRepository;

    private UserManager() {
        userRepository = UserRepository.getInstance();
        userAuthRepository = UserAuthRepository.getInstance();
    }

    public static UserManager getInstance() {
        UserManager result = instance;
        if (result != null) {
            return result;
        }
        synchronized(UserRepository.class) {
            if (instance == null) {
                instance = new UserManager();
            }
            return instance;
        }
    }

    public void createUser(){
        userRepository.createUser();
    }

    public Task<User> getUserData(){
        // Get the user from Firestore and cast it to a User model Object
        return userRepository.getUserData().continueWith(task -> task.getResult().toObject(User.class)) ;
    }

    public Task<IntegrationAuth> getIntegrationAuthData(IntegrationType type) {
        switch(type) {
            case PHILLIPS_HUE:
                return userAuthRepository.getIntegrationAuth(type)
                        .continueWith(task -> task.getResult().toObject(PhillipsHueIntegrationAuth.class));
            case NANOLEAF:
            default:
                return null;
        }
    }

    public Task<Void> updateUsername(String username){
        return userRepository.updateUsername(username);
    }

    public Task<Void> deleteUser(Context context){
        // Delete the user account from the Firestore
        String uid = this.getCurrentUser().getUid();
        return userRepository.deleteUserFromFirestore().addOnCompleteListener(task -> {
            // Once done, delete the user data from Auth
            userRepository.deleteUser(context);
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

}
