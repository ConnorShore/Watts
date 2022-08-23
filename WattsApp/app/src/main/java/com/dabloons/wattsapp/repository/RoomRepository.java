package com.dabloons.wattsapp.repository;

import android.content.Context;
import android.hardware.lights.Light;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.ui.main.MainActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.dabloons.wattsapp.model.User;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import util.WattsCallback;
import util.WattsCallbackStatus;

public final class RoomRepository {

    private final String LOG_TAG = "RoomRepository";

    private static volatile RoomRepository instance;

    private UserManager userManager = UserManager.getInstance();

    private static final String ROOM_COLLECTION_NAME = "rooms";

    private RoomRepository() { }

    public static RoomRepository getInstance() {
        RoomRepository result = instance;
        if (result != null) {
            return result;
        }
        synchronized(UserRepository.class) {
            if (instance == null) {
                instance = new RoomRepository();
            }
            return instance;
        }
    }

    // Create Room in Firestore
    public Room createRoom(String roomName) {
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString();
        Room roomToCreate = new Room(uid, userManager.getCurrentUser().getUid(), roomName);

        CollectionReference roomCollection = getRoomCollection();
        roomCollection.document(uid).set(roomToCreate);

        return roomToCreate;

    }

    public void getUserDefinedRooms(WattsCallback<ArrayList<Room>, Void> callback){
        ArrayList<Room> ret = new ArrayList<>();
        CollectionReference roomCollection = getRoomCollection();
        roomCollection.whereEqualTo("userId", userManager.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                   @Override
                   public void onComplete(@NonNull Task<QuerySnapshot> task) {
                       if (task.isSuccessful()) {
                           for (QueryDocumentSnapshot document : task.getResult()) {
                               //                                    Log.d(TAG, document.getId() + " => " + document.getData());
                               Room room = (Room) document.toObject(Room.class);
                               ret.add(room);
                           }
                           callback.apply(ret, new WattsCallbackStatus(true));
                       } else {
                           callback.apply(null, new WattsCallbackStatus(false, "Failed to getUserDefinedLights"));
                       }
                   }
                });
    }

    public void deleteRoom(String roomId)
    {
        getRoomCollection().document(roomId)
                .delete();
    }

    private CollectionReference getRoomCollection(){
        return FirebaseFirestore.getInstance().collection(ROOM_COLLECTION_NAME);
    }

}