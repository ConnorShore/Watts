package com.dabloons.wattsapp.ui.room;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.LightManager;
import com.dabloons.wattsapp.manager.RoomManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.repository.RoomRepository;
import com.dabloons.wattsapp.ui.room.adapters.LightItemAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import util.UIUtil;
import util.WattsCallback;

public class ModalBottomSheet extends BottomSheetDialogFragment
{

    private MaterialAlertDialogBuilder alertDialogBuilderEditRoomName;
    private View customDialogViewEditRoomName;

    private Room currRoom;
    private MaterialToolbar toolbar;
    private WattsCallback<List<Light>> onLightAddedCallback;

    private LightItemAdapter lightItemAdapter;
    private RecyclerView lightRV;
    private MaterialAlertDialogBuilder alertDialogBuilder;
    private View customDialogView;


    public ModalBottomSheet() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable
            ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.room_options_bottom_sheet,
                container, false);

        Bundle bundle = getArguments();
        currRoom = bundle.getParcelable("currRoom");
        toolbar = getActivity().findViewById(R.id.topAppBarRoomActivity);
        Button addLight = v.findViewById(R.id.addLight);
        Button editRoomName = v.findViewById(R.id.editRoomName);
        Button deleteRoom = v.findViewById(R.id.deleteRoom);

        alertDialogBuilder = new MaterialAlertDialogBuilder(getContext());
        alertDialogBuilderEditRoomName = new MaterialAlertDialogBuilder(getContext());

        customDialogView = LayoutInflater.from(getContext()).inflate(R.layout.add_light_to_room_dialog, null, false);
        lightRV = customDialogView.findViewById(R.id.lightRV);

        deleteRoom.setOnClickListener(view -> {
            RoomManager.getInstance().deleteRoom(currRoom, (var, status) -> {
                getActivity().finish();
            });
        });

        editRoomName.setOnClickListener(v1 -> {
            customDialogViewEditRoomName = LayoutInflater.from(getContext()).inflate(R.layout.edit_room_name_dialog, null, false);
            launchCustomAlertDialog();
        });

        addLight.setOnClickListener(v1 -> {
            launchCustomAlertDialogAddLight();
            ChipGroup integrationChipGroup = customDialogView.findViewById(R.id.integrationChipGroup);
            UIUtil.addIntegrationChips(customDialogView,integrationChipGroup);
            LightManager.getInstance().getLights((lights, success) -> {
                List<Light> lightList = getLightsNotInRoom(lights);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(WattsApplication.getAppContext(), LinearLayoutManager.VERTICAL, false);
                lightItemAdapter = new LightItemAdapter(WattsApplication.getAppContext(), (ArrayList<Light>) lightList);
                lightRV.setLayoutManager(linearLayoutManager);
                lightRV.setAdapter(lightItemAdapter);
                lightItemAdapter.notifyDataSetChanged();


            });

            integrationChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if(checkedIds.size() == 0)
                {
                    lightItemAdapter.setLights(new ArrayList());
                    UIUtil.updateUIChips(lightItemAdapter);
                }
                else if(checkedIds.size() == group.getChildCount())
                {
                    LightManager.getInstance().getLights((lights, success) -> {
                        List<Light> lightList = getLightsNotInRoom(lights);
                        setSelectedLights(lightList);
                        lightItemAdapter.setLights(lightList);
                        UIUtil.updateUIChips(lightItemAdapter);
                    });
                }
                else {
                    for (int id : checkedIds) {
                        List<Light> integrationlights = lightItemAdapter.getLightsFromIntegrationMap(IntegrationType.values()[id]);
                        List<Light> lightList = getLightsNotInRoom(integrationlights);
                        setSelectedLights(lightList);
                        lightItemAdapter.setLights(lightList);
                        UIUtil.updateUIChips(lightItemAdapter);
                    }
                }
            });
        });


        return  v;
    }

    private void setSelectedLights(List<Light> lights)
    {
        for(int i = 0; i < lights.size(); i++)
        {
            if (lightItemAdapter.currSelectedlightItems.containsKey(lights.get(i).getUid())) {
                lights.get(i).setSelected(true);
            }
        }
    }

    private List<Light> getLightsNotInRoom(List<Light> lights)
    {
        if(lights == null)
        {
            return new ArrayList<>();
        }
        List<Light> lightsToShow = new ArrayList<>();
        for(int i = 0; i < lights.size(); i++)
        {
            if(! currRoom.getLightIds().contains(lights.get(i).getUid())) {
                lightsToShow.add(lights.get(i));
            }
        }
        return lightsToShow;
    }

    private void launchCustomAlertDialogAddLight()
    {
        if(customDialogView.getParent() != null)
        {
            ((ViewGroup)customDialogView.getParent()).removeView(customDialogView);
        }
        alertDialogBuilder.setView(customDialogView)
                .setTitle("Add Lights")
                .setPositiveButton("Add", (dialog, which) -> {
                    List<Light> lightsToAdd = new ArrayList<>(lightItemAdapter.currSelectedlightItems.values());
//                    currRoom.
                    for(Light l : lightsToAdd)
                    {
                        RoomRepository.getInstance().addLightToRoom(currRoom, l.getUid());
                    }

                    onLightAddedCallback.apply(lightsToAdd);
                    dialog.dismiss();


                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    public void setOnLightAddedCallback(WattsCallback<List<Light>> callback) {
        onLightAddedCallback = callback;
    }

    private void launchCustomAlertDialog()
    {
        TextInputLayout roomName = customDialogViewEditRoomName.findViewById(R.id.editRoomNameTextLayout);
        alertDialogBuilderEditRoomName.setView(customDialogViewEditRoomName);
        alertDialogBuilderEditRoomName.setTitle("Enter Room Name");
        alertDialogBuilderEditRoomName.setPositiveButton("Set", (dialog, which) ->
        {
            updateRoomName(currRoom, roomName.getEditText().getText().toString());
            toolbar.setTitle(roomName.getEditText().getText().toString());
            dialog.dismiss();

        }).setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        }).show();
    }

    private void updateRoomName(Room currRoom, String name)
    {
        RoomManager.getInstance().updateRoomName(currRoom, name);

    }
}
