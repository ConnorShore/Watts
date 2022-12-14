package com.dabloons.wattsapp.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.Room;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import util.FirestoreUtil;
import util.WattsCallback;
import util.WattsCallbackStatus;

public final class RoomRepository {

    private final String LOG_TAG = "RoomRepository";

    private static volatile RoomRepository instance;

    private UserManager userManager = UserManager.getInstance();

    private final String ROOM_COLLECTION_NAME = WattsApplication.getResourceString(R.string.collection_rooms);

    private final String USER_ID_FIELD = WattsApplication.getResourceString(R.string.field_userId);
    private final String ROOM_NAME_FIELD = WattsApplication.getResourceString(R.string.field_name);
    private final String LIGHT_IDS_FIELD = WattsApplication.getResourceString(R.string.field_light_ids);
    private final String INTEGRATION_ID_FIELD = WattsApplication.getResourceString(R.string.field_integrationId);

    private RoomRepository() { }

    // Create Room in Firestore
    public void createRoom(String roomName, WattsCallback<Room> callback) {
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString();

        Room roomToCreate = new Room(uid, userManager.getCurrentUser().getUid(), roomName);

        CollectionReference roomCollection = getRoomCollection();
        roomCollection.document(uid).set(roomToCreate).addOnCompleteListener(task -> {
            if(task.isComplete())
                callback.apply(roomToCreate);
            else
                callback.apply(roomToCreate, new WattsCallbackStatus("Failed to add room."));
        }).addOnFailureListener(e -> callback.apply(roomToCreate, new WattsCallbackStatus(e.getMessage())));

    }

    public Task<Void> updateRoom(Room room) {
        return getRoomCollection().document(room.getUid()).update(LIGHT_IDS_FIELD, room.getLightIds());
    }

    public Task<Void> updateRoomName(Room room, String name) {
        return getRoomCollection().document(room.getUid()).update(ROOM_NAME_FIELD, name);
    }

    public Task<Void> setRoomIntegrationId(String roomUid, String id) {
        return getRoomCollection().document(roomUid).update(INTEGRATION_ID_FIELD, id);
    }

    public Task<Void> setRoomLights(Room room, List<String> lightIds) {
        Set<String> ids = new HashSet<>();
        ids.addAll(room.getLightIds());
        ids.addAll(lightIds);
        List<String> finalIds = new ArrayList<>(ids);
        room.setLightIds(finalIds);
        return updateRoom(room);
    }

    public Task<Void> addLightToRoom(Room room, String lightId) {
        room.appendLightId(lightId);
        return updateRoom(room);
    }


    public void getUserDefinedRooms(WattsCallback<ArrayList<Room>> callback){
        ArrayList<Room> ret = new ArrayList<>();
        CollectionReference roomCollection = getRoomCollection();
        roomCollection.whereEqualTo(USER_ID_FIELD, userManager.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Room room = (Room) document.toObject(Room.class);
                            ret.add(room);
                        }
                        callback.apply(ret);
                    } else {
                        callback.apply(null, new WattsCallbackStatus("Failed to getUserDefinedLights"));
                    }
                });
    }

    public Task<Void> deleteRoom(String roomId)
    {
        return getRoomCollection().document(roomId).delete();
    }
    public void deleteRoomsForUser(WattsCallback<Void> callback) {
        FirestoreUtil.deleteDocumentsForUser(getRoomCollection(), callback);
    }

    private CollectionReference getRoomCollection(){
        return FirebaseFirestore.getInstance().collection(ROOM_COLLECTION_NAME);
    }

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

}