package com.dabloons.wattsapp.ui.room;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.IntegrationSceneManager;
import com.dabloons.wattsapp.manager.LightManager;
import com.dabloons.wattsapp.manager.RoomManager;
import com.dabloons.wattsapp.manager.SceneManager;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.model.Scene;
import com.dabloons.wattsapp.model.integration.IntegrationAuth;
import com.dabloons.wattsapp.model.integration.IntegrationScene;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.ui.main.fragment.HomeFragment;
import com.dabloons.wattsapp.ui.room.adapters.LightAdapter;
import com.dabloons.wattsapp.ui.room.adapters.SceneAdapter;
import com.dabloons.wattsapp.ui.room.adapters.SceneDropdownAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import util.ItemOffsetDecoration;
import util.UIMessageUtil;
import util.UIUtil;
import util.WattsCallback;
import util.WattsCallbackStatus;


public class RoomActivity extends AppCompatActivity {

    private final String LOG_TAG = "RoomActivity";

    private Button addSceneBtn;
    private Button openBottomSheetBtn;

    private Room currentRoom;
    private MaterialToolbar toolbar;

    private MaterialAlertDialogBuilder alertDialogBuilder;
    private View customDialogView;



    private RecyclerView lightRV;
    private LightAdapter lightAdapter;
    private TextView lightPlaceHolderHeader;
    private TextView lightPlaceHolderBody;

    private RecyclerView sceneRV;
    private SceneAdapter sceneAdapter;
    private TextView scenePlaceHolderHeader;
    private TextView scenePlaceHolderBody;

    private RecyclerView sceneDropdownRV;
    private SceneDropdownAdapter sceneDropdownAdapter;

    private RoomManager roomManager = RoomManager.getInstance();
    private SceneManager sceneManager = SceneManager.getInstance();
    private LightManager lightManager = LightManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_room);

        addSceneBtn = findViewById(R.id.addSceneBtn);
        openBottomSheetBtn = findViewById(R.id.openBottomSheet);
        toolbar = findViewById(R.id.topAppBarRoomActivity);

        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            currentRoom = extras.getParcelable("room");
            toolbar.setTitle(currentRoom.getName());
        }

        //tool bar back button listener
        toolbar.setNavigationOnClickListener(v -> finish());

        openBottomSheetBtn.setOnClickListener(v -> {

            launchBottomSheet();
        });



        lightManager.getLightsForIds(currentRoom.getLightIds(), (lights, status) -> {
            lightAdapter = new LightAdapter(WattsApplication.getAppContext(), lights);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(WattsApplication.getAppContext(), 2, GridLayoutManager.HORIZONTAL, false);

            lightRV = findViewById(R.id.roomLightRV);
            lightPlaceHolderHeader = findViewById(R.id.emptyRoomLightListTextHeader);
            lightPlaceHolderBody = findViewById(R.id.emptyRoomLightListTextBody);

            UIUtil.toggleViews(lights.size(), lightPlaceHolderHeader, lightRV);
            UIUtil.toggleViews(lights.size(), lightPlaceHolderBody, lightRV);

            lightRV.setLayoutManager(gridLayoutManager);
            lightRV.setAdapter(lightAdapter);
            lightRV.addItemDecoration(new ItemOffsetDecoration(getApplicationContext(),R.dimen.light_card_offset));
            registerForContextMenu(lightRV);
        });

        sceneManager.getAllScenes(currentRoom.getUid(), (scenes, status) -> {

            sceneAdapter = new SceneAdapter(WattsApplication.getAppContext(), scenes);
            GridLayoutManager gridLayoutManager1 = new GridLayoutManager(WattsApplication.getAppContext(), 2, GridLayoutManager.HORIZONTAL, false);
            sceneRV = findViewById(R.id.roomSceneRV);
            scenePlaceHolderHeader = findViewById(R.id.emptyRoomSceneListTextHeader);
            scenePlaceHolderBody = findViewById(R.id.emptyRoomSceneListTextBody);
            sceneRV.setLayoutManager(gridLayoutManager1);
            sceneRV.setAdapter(sceneAdapter);
            sceneRV.addItemDecoration(new ItemOffsetDecoration(this.getApplicationContext(),R.dimen.light_card_offset));

            UIUtil.toggleViews(scenes.size(), scenePlaceHolderHeader, sceneRV);
            UIUtil.toggleViews(scenes.size(), scenePlaceHolderBody, sceneRV);

            registerForContextMenu(sceneRV);
        });

        alertDialogBuilder = new MaterialAlertDialogBuilder(RoomActivity.this);
        roomManager.getRoomIntegrationTypes(currentRoom, (integrationTypes, status) -> {
            if(!status.success) {
                Log.e(LOG_TAG, status.message);
                return;
            }

            IntegrationSceneManager.getInstance().getIntegrationScenesMap(integrationTypes, (map, status1) -> {
                if(!status1.success) {
                    Log.e(LOG_TAG, "Failed to get integration scene map");
                    return;
                }
                sceneDropdownAdapter = new SceneDropdownAdapter(map);
                initializeListeners();
            });
        });
    }

    private void launchBottomSheet()
    {
        ModalBottomSheet bottomSheet = new ModalBottomSheet();
        bottomSheet.setOnLightAddedCallback((lights, status) -> {
            lightAdapter.lights.addAll(lights);
            RoomManager.getInstance().addLightsToRoom(currentRoom, lights, (var, status1) -> updateUI());
        });
        Bundle roomParcel = new Bundle();
        roomParcel.putParcelable("currRoom", currentRoom);
        bottomSheet.setArguments(roomParcel);
        bottomSheet.show(getSupportFragmentManager(), "ModalBottomSheet");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getGroupId()) {
            case R.id.ctx_menu_group_lights:
                handleLightContextMenuClick(item, lightAdapter.getPosition());
                break;
            case R.id.ctx_menu_group_scenes:
                handleSceneContextMenuClick(item, sceneAdapter.getPosition());
                break;
        }

        return super.onContextItemSelected(item);
    }

    private void handleLightContextMenuClick(MenuItem item, int position) {
        switch(item.getItemId()) {
            case R.id.ctx_menu_item_details:
                // Todo: Open light details dialog
                break;
            case R.id.ctx_menu_item_delete:
                Light light = lightAdapter.lights.remove(position);
                roomManager.removeLightFromRoom(currentRoom, light, (var, status) -> {
                    if(status.success) {
                        UIMessageUtil.showShortToastMessage(getApplicationContext(), "Successfully removed light");
                        updateUI();
                    }
                    else
                        UIMessageUtil.showShortToastMessage(getApplicationContext(), "Failed to remove light");
                });
                break;
        }
    }


    private void handleSceneContextMenuClick(MenuItem item, int position) {
        switch(item.getItemId()) {
            case R.id.ctx_menu_item_details:
                // Todo: Open scene details dialog
                break;
            case R.id.ctx_menu_item_delete:
                Scene scene = sceneAdapter.scenes.remove(position);
                sceneManager.deleteScene(scene, (var, status) -> {
                    if(status.success) {
                        UIMessageUtil.showShortToastMessage(getApplicationContext(), "Successfully deleted scene");
                        sceneAdapter.scenes.remove(scene);
                        updateUI();
                    }
                    else
                        UIMessageUtil.showShortToastMessage(getApplicationContext(), "Failed to delete scene");
                });
                break;
        }
    }

    private void initializeListeners() {
        addSceneBtn.setOnClickListener(v -> {
            customDialogView = LayoutInflater.from(RoomActivity.this).inflate(R.layout.create_scene_dialog, null, false);
            launchCustomAlertDialog();
        });
    }

    private void launchCustomAlertDialog() {
        alertDialogBuilder.setView(customDialogView);
        sceneDropdownRV = customDialogView.findViewById(R.id.sceneDropdownRV);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(WattsApplication.getAppContext(), LinearLayoutManager.VERTICAL, false);
        sceneDropdownRV.setLayoutManager(linearLayoutManager);
        sceneDropdownRV.addItemDecoration(new ItemOffsetDecoration(this.getApplicationContext(),R.dimen.light_card_offset));
        sceneDropdownRV.setAdapter(sceneDropdownAdapter);
        alertDialogBuilder.setTitle("Create Scene");
        alertDialogBuilder.setPositiveButton("Add", (dialog, which) -> {
            addSceneToUser();
        }).setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        }).show();
    }

    private void addSceneToUser() {
        Map<IntegrationAuth, IntegrationScene> scenes = sceneDropdownAdapter.getSelectedScenes();
        ArrayList<IntegrationScene> scenesToAdd = new ArrayList<>();
        for(Map.Entry<IntegrationAuth, IntegrationScene> scene : scenes.entrySet())
        {
            scenesToAdd.add(scene.getValue());
        }
        TextInputLayout sceneName = customDialogView.findViewById(R.id.sceneNameTextLayout);
        SceneManager.getInstance().createScene(currentRoom.getUid(), sceneName.getEditText().getText().toString() ,scenesToAdd, (var, status) -> {
            if(status.success) {
                sceneAdapter.scenes.add(var);
                UIMessageUtil.showShortToastMessage(WattsApplication.getAppContext(), "Successfully added scene");
            }
            else
                UIMessageUtil.showShortToastMessage(WattsApplication.getAppContext(), "Failed to add scene");

            UIUtil.toggleViews(scenes.size(), scenePlaceHolderHeader, sceneRV);
            UIUtil.toggleViews(scenes.size(), scenePlaceHolderBody, sceneRV);
            updateUI();
        });
    }

    public void updateUI()
    {
        new Handler(Looper.getMainLooper()).post(() -> {
            sceneAdapter.notifyDataSetChanged();
            lightAdapter.notifyDataSetChanged();
        });
    }
}