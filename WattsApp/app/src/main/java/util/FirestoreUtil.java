package util;

import android.util.Log;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.UserManager;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class FirestoreUtil {

    private static String LOG_TAG = "FirestoreUtil";
    private static final String USER_ID_FIELD = WattsApplication.getResourceString(R.string.field_userId);

    public static void deleteDocumentsForUser(CollectionReference collection, WattsCallback<Void, Void> callback) {
        FirebaseUser user = UserManager.getInstance().getCurrentUser();
        if(user == null) return;

        getUserDocIds(collection, (docIds, status) -> {
            WriteBatch batch = FirebaseFirestore.getInstance().batch();
            for(String id : docIds) {
                batch.delete(collection.document(id));
            }

            batch.commit()
                    .addOnCompleteListener(task ->{
                        callback.apply(null, new WattsCallbackStatus(true));
                    })
                    .addOnFailureListener(task -> {
                        callback.apply(null, new WattsCallbackStatus(false, task.getMessage()));
                    });

            return null;
        });

    }

    private static void getUserDocIds(CollectionReference collection, WattsCallback<List<String>, Void> callback) {
        String userId = UserManager.getInstance().getCurrentUser().getUid();
        collection.get().addOnCompleteListener(task -> {
            if(!task.isComplete())
                Log.e(LOG_TAG, "Failed to get lights collection");

            List<String> ids = new ArrayList<>();
            for (QueryDocumentSnapshot document : task.getResult()) {
                boolean userEqual = document.get(USER_ID_FIELD).toString().equals(userId);
                if(userEqual)
                    ids.add(document.get("userId").toString());
            }

            callback.apply(ids, new WattsCallbackStatus(true));
        });
    }
}
