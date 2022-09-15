package com.dabloons.wattsapp.repository;

import android.util.Log;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.integration.IntegrationAuth;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.model.integration.NanoleafPanelAuthCollection;
import com.dabloons.wattsapp.model.integration.PhillipsHueIntegrationAuth;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

import util.FirestoreUtil;
import util.RepositoryUtil;
import util.WattsCallback;
import util.WattsCallbackStatus;

public final class UserAuthRepository {

    private final String LOG_TAG = "UserAuthRepository";

    private static volatile UserAuthRepository instance;

    private static final String USER_COLLECTION_NAME = WattsApplication.getResourceString(R.string.collection_users);
    private static final String AUTH_COLLECTION_NAME = WattsApplication.getResourceString(R.string.collection_auth);

    private static final String INTEGRATION_TYPE_PROPERTY = WattsApplication.getResourceString(R.string.field_integration_type);

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

    public void getUserIntegrations(WattsCallback<List<IntegrationType>> callback) {
        this.getUserAuthCollection().get().addOnCompleteListener(task -> {
            if(!task.isComplete())
                Log.e(LOG_TAG, "Failed to get lights collection");

            List<IntegrationType> ret = new ArrayList<>();
            for (QueryDocumentSnapshot document : task.getResult()) {
                IntegrationType type = RepositoryUtil.stringToIntegrationType((String)document.get("integrationType"));
                ret.add(type);
            }

            callback.apply(ret);
        });
    }

    public Task<Void> addPhillipsHueIntegrationToUser(PhillipsHueIntegrationAuth authProps) {
        FirebaseUser user = UserRepository.getInstance().getCurrentUser();
        if(user == null) return null;
        return setIntegrationAuth(IntegrationType.PHILLIPS_HUE, authProps);
    }

    public Task<Void> addNanoleafIntegrationToUser(NanoleafPanelAuthCollection collection) {
        FirebaseUser user = UserRepository.getInstance().getCurrentUser();
        if(user == null) return null;
        return setIntegrationAuth(IntegrationType.NANOLEAF, collection);
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

    public void deleteIntegrationsForUser(WattsCallback<Void> callback) {
        deleteIntegrationssForUser(callback);
    }

    private void deleteIntegrationssForUser(WattsCallback<Void> callback) {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null) return;

        getAllDocByProp((docIds, status) -> {
            WriteBatch batch = FirebaseFirestore.getInstance().batch();
            for(String id : docIds) {
                batch.delete(getUserAuthCollection().document(id));
            }

            batch.commit()
                    .addOnCompleteListener(task ->{
                        callback.apply(null);
                    })
                    .addOnFailureListener(task -> {
                        callback.apply(null, new WattsCallbackStatus(task.getMessage()));
                    });
        });
    }

    private void getAllDocByProp(WattsCallback<List<String>> callback) {
        getUserAuthCollection().get().addOnCompleteListener(task -> {
            if(!task.isComplete())
                Log.e(LOG_TAG, "Failed to get lights collection");

            List<String> ids = new ArrayList<>();
            for (QueryDocumentSnapshot document : task.getResult()) {
                ids.add(document.get(INTEGRATION_TYPE_PROPERTY).toString().toLowerCase());
            }

            callback.apply(ids);
        });
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
