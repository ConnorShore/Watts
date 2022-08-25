package com.dabloons.wattsapp.ui.room;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.RoomManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.ui.room.adapters.LightAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

import util.ItemOffsetDecoration;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class RoomActivity extends AppCompatActivity {
    private Button deleteRoomBtn;
    private Room currentRoom;
//    private MaterialToolbar toolbar;

    private RecyclerView lightRV;
    private LightAdapter lightAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_room);

        deleteRoomBtn = findViewById(R.id.deletRoomButton);
//        toolbar = findViewById(R.id.topAppBarRoomActivity);




        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            currentRoom = extras.getParcelable("room");

//            toolbar.setTitle(currentRoom.getName());
        }

//        this.setSupportActionBar(toolbar);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });

        lightAdapter = new LightAdapter(WattsApplication.getAppContext(), (ArrayList<Light>) currentRoom.getLights());
        GridLayoutManager linearLayoutManager = new GridLayoutManager(WattsApplication.getAppContext(), 2, GridLayoutManager.HORIZONTAL, false);

        lightRV = findViewById(R.id.roomLightRV);
        lightRV.setLayoutManager(linearLayoutManager);
        lightRV.setAdapter(lightAdapter);
        lightRV.addItemDecoration(new ItemOffsetDecoration(this.getApplicationContext(),R.dimen.light_card_offset));


        deleteRoomBtn.setOnClickListener(v -> {
            RoomManager.getInstance().deleteRoom(currentRoom.getUid(), (var, status) -> {
                finish();
                return null;
            });

        });
    }
}