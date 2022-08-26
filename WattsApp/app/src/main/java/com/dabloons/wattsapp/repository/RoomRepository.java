package com.dabloons.wattsapp.repository;

import androidx.annotation.NonNull;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.Room;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
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

    private final String ROOM_COLLECTION_NAME = WattsApplication.getResourceString(R.string.collection_rooms);

    private final String USER_ID_FIELD = WattsApplication.getResourceString(R.string.field_userId);
    private final String LIGHTS_FIELD = WattsApplication.getResourceString(R.string.field_lights);
    private final String INTEGRATION_ID_FIELD = WattsApplication.getResourceString(R.string.field_integrationId);

    private RoomRepository() { }

    // Create Room in Firestore
    public void createRoom(String roomName, WattsCallback<Room, Void> callback) {
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString();

        Room roomToCreate = new Room(uid, userManager.getCurrentUser().getUid(), roomName);

        CollectionReference roomCollection = getRoomCollection();
        roomCollection.document(uid).set(roomToCreate).addOnCompleteListener(task -> {
            if(task.isComplete())
                callback.apply(roomToCreate, new WattsCallbackStatus(true));
            else
                callback.apply(roomToCreate, new WattsCallbackStatus(false, "Failed to add room."));
        }).addOnFailureListener(e -> callback.apply(roomToCreate, new WattsCallbackStatus(false, e.getMessage())));

    }

    public Task<Void> updateRoom(Room room) {
        return getRoomCollection().document(room.getUid()).update(LIGHTS_FIELD, room.getLights());
    }

    public Task<Void> setRoomIntegrationId(String roomUid, String id) {
        return getRoomCollection().document(roomUid).update(INTEGRATION_ID_FIELD, id);
    }

    public Task<Void> addLightsToRoom(Room room, List<Light> lights) {
        room.setLights(lights);
        return updateRoom(room);
    }

    public void getUserDefinedRooms(WattsCallback<ArrayList<Room>, Void> callback){
        ArrayList<Room> ret = new ArrayList<>();
        CollectionReference roomCollection = getRoomCollection();
        roomCollection.whereEqualTo(USER_ID_FIELD, userManager.getCurrentUser().getUid())
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

    public Task<Void> deleteRoom(String roomId, WattsCallback<Void, Void> callback)
    {
        return getRoomCollection().document(roomId).delete();
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