package com.dabloons.wattsapp.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.dabloons.wattsapp.model.User;
import com.dabloons.wattsapp.model.integration.IntegrationAuth;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.model.integration.PhillipsHueIntegrationAuth;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public final class UserAuthRepository {

    private static volatile UserAuthRepository instance;

    private static final String USER_COLLECTION_NAME = "users";
    private static final String AUTH_COLLECTION_NAME = "auth";

    private static final String DOCUMENT_PHILLIPS_HUE = "phillips_hue";
    private static final String DOCUMENT_NANOLEAF = "nanoleaf";

    private UserAuthRepository() { }

    public static UserAuthRepository getInstance() {
        UserAuthRepository result = instance;
        if (result != null) {
            return result;
        }
        synchronized(UserAuthRepository.class) {
            if (instance == null) {
                instance = new UserAuthRepository();
            }
            return instance;
        }
    }

    public Task<Void> addPhillipsHueIntegrationToUser(String accessToken, String refreshToken, String username) {
        FirebaseUser user = UserRepository.getInstance().getCurrentUser();
        if(user == null) return null;

        PhillipsHueIntegrationAuth authProps =
                new PhillipsHueIntegrationAuth(UUID.randomUUID().toString(), username, accessToken, refreshToken);

        return addIntegrationAuth(IntegrationType.PHILLIPS_HUE, authProps);
    }

    public Task<Void> removeIntegration(IntegrationType type) {
        String doc = getIntegrationDocument(type);
        return this.getUserAuthCollection().document(doc).delete();
    }

    public Task<Void> updatePropertyString(String prop, String value, IntegrationType type) {
        String doc = getIntegrationDocument(type);
        return this.getUserAuthCollection().document(doc).update(prop, value);
    }

    public Task<Void> updatePropertyInteger(String prop, int value, IntegrationType type) {
        String doc = getIntegrationDocument(type);
        return this.getUserAuthCollection().document(doc).update(prop, value);
    }

    public Task<Void> updatePropertyBoolean(String prop, boolean value, IntegrationType type) {
        String doc = getIntegrationDocument(type);
        return this.getUserAuthCollection().document(doc).update(prop, value);
    }

    public Task<DocumentSnapshot> getIntegrationAuth(IntegrationType type) {
        String doc = getIntegrationDocument(type);
        return this.getUserAuthCollection().document(doc).get();
    }

    public Task<Void> addIntegrationAuth(IntegrationType type, IntegrationAuth props) {
        String doc = getIntegrationDocument(type);
        return this.getUserAuthCollection().document(doc).set(props);
    }

    private String getIntegrationDocument(IntegrationType type) {
        switch (type) {
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
        String uid = UserRepository.getInstance().getCurrentUserUID();
        CollectionReference ref = FirebaseFirestore.getInstance()
                .collection(USER_COLLECTION_NAME)
                .document(uid)
                .collection(AUTH_COLLECTION_NAME);
        return ref;
    }
}
