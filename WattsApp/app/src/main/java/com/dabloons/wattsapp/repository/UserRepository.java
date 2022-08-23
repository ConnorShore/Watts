package com.dabloons.wattsapp.repository;

import android.content.Context;

import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.dabloons.wattsapp.model.User;

public final class UserRepository {

    private final String LOG_TAG = "UserRepository";

    private static volatile UserRepository instance;

    private static final String USER_COLLECTION_NAME = "users";
    private static final String FIELD_USERNAME = "username";

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
}