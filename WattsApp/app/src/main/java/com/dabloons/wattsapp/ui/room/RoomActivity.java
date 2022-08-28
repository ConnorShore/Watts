package com.dabloons.wattsapp.ui.room;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.RoomManager;
import com.dabloons.wattsapp.manager.SceneManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.model.Scene;
import com.dabloons.wattsapp.ui.room.adapters.LightAdapter;
import com.dabloons.wattsapp.ui.room.adapters.SceneAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

import util.ItemOffsetDecoration;
import util.UIMessageUtil;

public class RoomActivity extends AppCompatActivity {
    private Button deleteRoomBtn;
    private Button addSceneBtn;
    private Room currentRoom;
    private MaterialToolbar toolbar;

    private RecyclerView lightRV;
    private LightAdapter lightAdapter;

    private RecyclerView sceneRV;
    private SceneAdapter sceneAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_room);

        deleteRoomBtn = findViewById(R.id.deletRoomButton);
        addSceneBtn = findViewById(R.id.addSceneBtn);
        toolbar = findViewById(R.id.topAppBarRoomActivity);

        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            currentRoom = extras.getParcelable("room");
            toolbar.setTitle(currentRoom.getName());
        }

        //tool bar back button listener
        toolbar.setNavigationOnClickListener(v -> finish());

        lightAdapter = new LightAdapter(WattsApplication.getAppContext(), (ArrayList<Light>) currentRoom.getLights());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(WattsApplication.getAppContext(), 2, GridLayoutManager.HORIZONTAL, false);

        lightRV = findViewById(R.id.roomLightRV);
        lightRV.setLayoutManager(gridLayoutManager);
        lightRV.setAdapter(lightAdapter);
        lightRV.addItemDecoration(new ItemOffsetDecoration(this.getApplicationContext(),R.dimen.light_card_offset));

        initializeListeners();

        SceneManager.getInstance().getAllScenes(currentRoom.getUid(), (scenes, status) -> {

            sceneAdapter = new SceneAdapter(WattsApplication.getAppContext(), (ArrayList<Scene>) scenes);
            GridLayoutManager gridLayoutManager1 = new GridLayoutManager(WattsApplication.getAppContext(), 2, GridLayoutManager.HORIZONTAL, false);
            sceneRV = findViewById(R.id.roomSceneRV);
            sceneRV.setLayoutManager(gridLayoutManager1);
            sceneRV.setAdapter(sceneAdapter);
            sceneRV.addItemDecoration(new ItemOffsetDecoration(this.getApplicationContext(),R.dimen.light_card_offset));
            return null;
        });

    }

    private void initializeListeners() {


        addSceneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SceneManager.getInstance().createScene(currentRoom.getUid(), "TEST", new ArrayList<>(), (var, status) -> {
                    if(status.success) {
                        sceneAdapter.sceneArrayList.add(var);
                        updateUI();
                        UIMessageUtil.showShortToastMessage(WattsApplication.getAppContext(), "Successfully added scene");
                    }
                    else
                        UIMessageUtil.showShortToastMessage(WattsApplication.getAppContext(), "Failed to add scene");

                    return null;
                });
            }
        });

        deleteRoomBtn.setOnClickListener(v -> {
            RoomManager.getInstance().deleteRoom(currentRoom.getUid(), (var, status) -> {
                finish();
                return null;
            });

        });
    }

    private void updateUI()
    {
        new Handler(Looper.getMainLooper()).post(() -> sceneAdapter.notifyDataSetChanged());
    }
}