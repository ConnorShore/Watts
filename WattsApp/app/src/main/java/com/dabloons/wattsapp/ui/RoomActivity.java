package com.dabloons.wattsapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.manager.RoomManager;
import com.dabloons.wattsapp.model.Room;
import com.google.android.material.appbar.MaterialToolbar;

import util.WattsCallback;
import util.WattsCallbackStatus;

public class RoomActivity extends AppCompatActivity {
    private Button deleteRoomBtn;
    private Room currentRoom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_room);

        deleteRoomBtn = findViewById(R.id.deletRoomButton);
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            currentRoom = extras.getParcelable("room");
            MaterialToolbar toolbar = findViewById(R.id.topAppBar);
            toolbar.setTitle(currentRoom.getName());
        }
        deleteRoomBtn.setOnClickListener(v -> {
            RoomManager.getInstance().deleteRoom(currentRoom.getUid(), new WattsCallback<Void, Void>() {
                @Override
                public Void apply(Void var, WattsCallbackStatus status) {
                    finish();
                    return null;
                }
            });

        });
    }
}