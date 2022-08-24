package com.dabloons.wattsapp.repository;

import android.util.Log;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.model.integration.IntegrationAuth;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.model.integration.PhillipsHueIntegrationAuth;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import util.WattsCallback;
import util.WattsCallbackStatus;

public final class UserAuthRepository {

    private final String LOG_TAG = "UserAuthRepository";

    private static volatile UserAuthRepository instance;

    private static final String USER_COLLECTION_NAME = WattsApplication.getResourceString(R.string.collection_users);
    private static final String AUTH_COLLECTION_NAME = WattsApplication.getResourceString(R.string.collection_auth);

    private static final String DOCUMENT_PHILLIPS_HUE = WattsApplication.getResourceString(R.string.document_phillips_hue);
    private static final String DOCUMENT_NANOLEAF  = WattsApplication.getResourceString(R.string.document_nanoleaf);

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

    public void getUserIntegrations(WattsCallback<List<IntegrationType>, Void> callback) {
        this.getUserAuthCollection().get().addOnCompleteListener(task -> {
            if(!task.isComplete())
                Log.e(LOG_TAG, "Failed to get lights collection");

            List<IntegrationType> ret = new ArrayList<>();
            for (QueryDocumentSnapshot document : task.getResult()) {
                IntegrationType type = stringToIntegrationType((String)document.get("integrationType"));
                ret.add(type);
            }

            callback.apply(ret, new WattsCallbackStatus(true));
        });
    }

    public Task<Void> addPhillipsHueIntegrationToUser(String accessToken, String refreshToken, String username) {
        FirebaseUser user = UserRepository.getInstance().getCurrentUser();
        if(user == null) return null;

        PhillipsHueIntegrationAuth authProps =
                new PhillipsHueIntegrationAuth(UUID.randomUUID().toString(), username, accessToken, refreshToken);

        return setIntegrationAuth(IntegrationType.PHILLIPS_HUE, authProps);
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

    public Task<Void> setIntegrationAuth(IntegrationType type, IntegrationAuth props) {
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

    private IntegrationType stringToIntegrationType(String s) {
        switch(s) {
            case "PHILLIPS_HUE":
                return IntegrationType.PHILLIPS_HUE;
            case "NANOLEAF":
                return IntegrationType.NANOLEAF;
            default:
                return IntegrationType.NONE;
        }
    }
}
