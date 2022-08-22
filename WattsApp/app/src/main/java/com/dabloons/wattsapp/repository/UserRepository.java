package com.dabloons.wattsapp.repository;

import android.content.Context;

import androidx.annotation.Nullable;

import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.model.integration.PhillipsHueIntegrationAuth;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.dabloons.wattsapp.model.User;

import java.util.UUID;

public final class UserRepository {

    private static volatile UserRepository instance;

    private static final String USER_COLLECTION_NAME = "users";
    private static final String AUTH_COLLECTION_NAME = "auth";
    private static final String FIELD_USERNAME = "username";

    private static final String DOCUMENT_PHILLIPS_HUE = "phillips_hue";
    private static final String DOCUMENT_NANOLEAF = "nanoleaf";

    private UserRepository() { }

    public static UserRepository getInstance() {
        UserRepository result = instance;
        if (result != null) {
            return result;
        }
        synchronized(UserRepository.class) {
            if (instance == null) {
                instance = new UserRepository();
            }
            return instance;
        }
    }

    /*
    USER SPECIFIC METHODS
     */

    // Create User in Firestore
    public void createUser() {
        FirebaseUser user = getCurrentUser();
        if(user == null) return;

        String username = user.getDisplayName();
        String uid = user.getUid();

        User userToCreate = new User(uid, username);

        Task<DocumentSnapshot> userData = getUserData();
        // If the user already exist in Firestore, we get his data (isMentor)
        userData.addOnSuccessListener(documentSnapshot -> {
            this.getUsersCollection().document(uid).set(userToCreate);
        });
    }

    // Get current User UID, if we have a current user
    @Nullable
    public String getCurrentUserUID() {
        FirebaseUser currentUser = getCurrentUser();
        return (currentUser != null) ? currentUser.getUid() : null;
    }

    // Get User Data from Firestore
    public Task<DocumentSnapshot> getUserData(){
        String uid = this.getCurrentUserUID();
        if(uid != null){
            return this.getUsersCollection().document(uid).get();
        }else{
            return null;
        }
    }

    public Task<DocumentSnapshot> getAuthData(IntegrationType type) {
        String doc = getIntegrationDocument(type);
        return this.getUserAuthCollection().document(doc).get();
    }

    // Update User Username
    public Task<Void> updateUsername(String username) {
        String uid = this.getCurrentUserUID();
        if(uid != null){
            return this.getUsersCollection().document(uid).update(FIELD_USERNAME, username);
        }else{
            return null;
        }
    }

    public Task<Void> deleteUserFromFirestore() {
        String uid = this.getCurrentUserUID();
        if(uid == null)
            return null;

        return this.getUsersCollection().document(uid).delete();
    }

    public Task<Void> signOut(Context context){
        return AuthUI.getInstance().signOut(context);
    }

    public Task<Void> deleteUser(Context context){
        return AuthUI.getInstance().delete(context);
    }

    @Nullable
    public FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    // Get the User Collection Reference
    private CollectionReference getUsersCollection(){
        return FirebaseFirestore.getInstance().collection(USER_COLLECTION_NAME);
    }


    /*
    INTEGRATION AUTH Methods
     */


    public void addPhillipsHueIntegrationToUser(String authToken, String refreshToken, String username) {
        FirebaseUser user = getCurrentUser();
        if(user == null) return;

        PhillipsHueIntegrationAuth authProps =
                new PhillipsHueIntegrationAuth(UUID.randomUUID().toString(), user.getUid(), username, authToken, refreshToken);

        Task<DocumentSnapshot> authData = getAuthData(IntegrationType.PHILLIPS_HUE);
        authData.addOnSuccessListener(snapshot -> {
            this.getUserAuthCollection().document(DOCUMENT_PHILLIPS_HUE).set(authProps);
        });
    }

    // Update User Username
    public Task<Void> setUsername(String username) {
        String uid = this.getCurrentUserUID();
        if(uid != null){
            return this.getUsersCollection().document(uid).update(FIELD_USERNAME, username);
        }else{
            return null;
        }
    }

    public Task<String> getAuthCode(IntegrationType type) { return null; }

    private String getIntegrationDocument(IntegrationType type) {
        switch(type) {
            case PHILLIPS_HUE:
                return DOCUMENT_PHILLIPS_HUE;
            case NANOLEAF:
                return DOCUMENT_NANOLEAF;
            default:
                return null;
        }
    }


    // Get the Auth collection reference
    private CollectionReference getUserAuthCollection() {
        String uid = this.getCurrentUserUID();
        return FirebaseFirestore.getInstance()
                .collection(USER_COLLECTION_NAME)
                .document(uid)
                .collection(AUTH_COLLECTION_NAME);
    }
}