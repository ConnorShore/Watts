package com.dabloons.wattsapp.ui.room;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.RoomManager;
import com.dabloons.wattsapp.manager.SceneManager;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.model.Scene;
import com.dabloons.wattsapp.model.integration.IntegrationScene;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.ui.room.adapters.LightAdapter;
import com.dabloons.wattsapp.ui.room.adapters.SceneAdapter;
import com.dabloons.wattsapp.ui.room.adapters.SceneDropdownAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import util.ItemOffsetDecoration;
import util.UIMessageUtil;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class RoomActivity extends AppCompatActivity {
    private Button deleteRoomBtn;
    private Button addSceneBtn;
    private Room currentRoom;
    private MaterialToolbar toolbar;

    private MaterialAlertDialogBuilder alertDialogBuilder;
    private View customDialogView;

    private RecyclerView lightRV;
    private LightAdapter lightAdapter;

    private RecyclerView sceneRV;
    private SceneAdapter sceneAdapter;

    private RecyclerView sceneDropdownRV;
    private SceneDropdownAdapter sceneDropdownAdapter;

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



        SceneManager.getInstance().getAllScenes(currentRoom.getUid(), (scenes, status) -> {

            sceneAdapter = new SceneAdapter(WattsApplication.getAppContext(), (ArrayList<Scene>) scenes);
            GridLayoutManager gridLayoutManager1 = new GridLayoutManager(WattsApplication.getAppContext(), 2, GridLayoutManager.HORIZONTAL, false);
            sceneRV = findViewById(R.id.roomSceneRV);
            sceneRV.setLayoutManager(gridLayoutManager1);
            sceneRV.setAdapter(sceneAdapter);
            sceneRV.addItemDecoration(new ItemOffsetDecoration(this.getApplicationContext(),R.dimen.light_card_offset));
            return null;
        });

        alertDialogBuilder = new MaterialAlertDialogBuilder(RoomActivity.this);
        UserManager.getInstance().getUserIntegrations((var, status) -> {

            sceneDropdownAdapter = new SceneDropdownAdapter(RoomActivity.this, (ArrayList<IntegrationType>) var);
            initializeListeners();
            return null;
        });

    }

    private void initializeListeners() {


        addSceneBtn.setOnClickListener(v -> {

            customDialogView = LayoutInflater.from(RoomActivity.this).inflate(R.layout.create_scene_dialog, null, false);
            launchCustomAlertDialog();
        });

        deleteRoomBtn.setOnClickListener(v -> {
            RoomManager.getInstance().deleteRoom(currentRoom.getUid(), (var, status) -> {
                finish();
                return null;
            });

        });
    }

    private void launchCustomAlertDialog() {
        alertDialogBuilder.setView(customDialogView);
        sceneDropdownRV = customDialogView.findViewById(R.id.sceneDropdownRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(WattsApplication.getAppContext(), LinearLayoutManager.VERTICAL, false);
        sceneDropdownRV.setLayoutManager(linearLayoutManager);
        sceneDropdownRV.addItemDecoration(new ItemOffsetDecoration(this.getApplicationContext(),R.dimen.light_card_offset));
        sceneDropdownRV.setAdapter(sceneDropdownAdapter);
        alertDialogBuilder.setPositiveButton("Add", (dialog, which) -> {
            Map<IntegrationType, IntegrationScene> scenes = sceneDropdownAdapter.getSelectedScenes();
            ArrayList<IntegrationScene> scenesToAdd = new ArrayList<>();
            for(Map.Entry<IntegrationType, IntegrationScene> scene : scenes.entrySet())
            {
                scenesToAdd.add(scene.getValue());
            }
            TextInputLayout sceneName = customDialogView.findViewById(R.id.sceneNameTextLayout);
            SceneManager.getInstance().createScene(currentRoom.getUid(),sceneName.getEditText().getText().toString() , scenesToAdd, (var, status) -> {
                if(status.success) {
                    sceneAdapter.sceneArrayList.add(var);
                    updateUI();
                    UIMessageUtil.showShortToastMessage(WattsApplication.getAppContext(), "Successfully added scene");
                }
                else
                    UIMessageUtil.showShortToastMessage(WattsApplication.getAppContext(), "Failed to add scene");

                return null;
            });
        }).setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        }).show();
    }

    private void updateUI()
    {
        new Handler(Looper.getMainLooper()).post(() -> sceneAdapter.notifyDataSetChanged());
    }
}