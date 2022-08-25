package com.dabloons.wattsapp.ui.main.room;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.RoomManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.Room;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

import util.SpacesItemDecoration;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class RoomActivity extends AppCompatActivity {
    private Button deleteRoomBtn;
    private Room currentRoom;

    private RecyclerView lightRV;
    private LightAdapter lightAdapter;
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

        lightAdapter = new LightAdapter(WattsApplication.getAppContext(), (ArrayList<Light>) currentRoom.getLights());
        GridLayoutManager linearLayoutManager = new GridLayoutManager(WattsApplication.getAppContext(), 2, GridLayoutManager.HORIZONTAL, false);

        lightRV = findViewById(R.id.roomLightRV);
        lightRV.setLayoutManager(linearLayoutManager);
        lightRV.setAdapter(lightAdapter);
//        lightRV.addItemDecoration(new SpacesItemDecoration(0));


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