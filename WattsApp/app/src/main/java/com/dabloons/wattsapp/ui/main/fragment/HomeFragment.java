package com.dabloons.wattsapp.ui.main.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.LightManager;
import com.dabloons.wattsapp.manager.RoomManager;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.repository.RoomRepository;
import com.dabloons.wattsapp.service.PhillipsHueService;
import com.dabloons.wattsapp.ui.room.RoomActivity;
import com.dabloons.wattsapp.ui.main.OnItemClickListener;
import com.dabloons.wattsapp.ui.room.adapters.LightItemAdapter;
import com.dabloons.wattsapp.ui.main.adapters.RoomAdapter;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import util.RequestCodes;
import util.UIUtil;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class HomeFragment extends Fragment implements OnItemClickListener {

    private final String LOG_TAG = "HomeFragment";

    private MaterialAlertDialogBuilder alertDialogBuilder;
    private View customDialogView;

    private RecyclerView roomRV;
    private RecyclerView lightRV;
    private TextView emptyViewTextHeader;
    private TextView emptyViewTextBody;

    // Arraylist for storing data
    private RoomAdapter roomAdapter;
    private LightItemAdapter mLightItemAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoomRepository.getInstance().getUserDefinedRooms((rooms, success) -> {
            roomAdapter = new RoomAdapter(WattsApplication.getAppContext(), rooms);

            UIUtil.toggleViews(rooms.size(), emptyViewTextHeader, roomRV);
            UIUtil.toggleViews(rooms.size(), emptyViewTextBody, roomRV);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(WattsApplication.getAppContext(), LinearLayoutManager.VERTICAL, false);

            // in below two lines we are setting layout manager and adapter to our recycler view.
            roomRV.setLayoutManager(linearLayoutManager);
            roomRV.setAdapter(roomAdapter);
            roomAdapter.setClickListener(this);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        roomRV = v.findViewById(R.id.idRVCourse);
        emptyViewTextHeader = v.findViewById(R.id.emptyRoomListTextHeader);
        emptyViewTextBody = v.findViewById(R.id.emptyRoomListTextBody);

// Debug button
//        Button b = v.findViewById(R.id.getGroups);
//        b.setOnClickListener(view -> {
//            RoomManager.getInstance().getAllPhillipsHueGroups();
//        });

        alertDialogBuilder = new MaterialAlertDialogBuilder(this.getContext());
        Button addRoom = (Button) v.findViewById(R.id.addRoomButton);
        addRoom.setOnClickListener(view -> {
            customDialogView = LayoutInflater.from(this.getContext()).inflate(R.layout.add_room_dialog, null, false);
            lightRV = customDialogView.findViewById(R.id.lightRV);
            launchCustomAlertDialog();
        });

        return v;
    }

    private void launchCustomAlertDialog()
    {
        TextInputLayout roomName = customDialogView.findViewById(R.id.roomNameTextLayout);
        ChipGroup integrationChipGroup = customDialogView.findViewById(R.id.integrationChipGroup);
        UIUtil.addIntegrationChips(customDialogView, integrationChipGroup);
        LightManager.getInstance().getLights((lights, success) -> {
            LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(WattsApplication.getAppContext(), LinearLayoutManager.VERTICAL, false);
            mLightItemAdapter = new LightItemAdapter(WattsApplication.getAppContext(), (ArrayList<Light>) lights);
            lightRV.setLayoutManager(linearLayoutManager1);
            lightRV.setAdapter(mLightItemAdapter);
            roomAdapter.setClickListener(HomeFragment.this::onClick);
        });

        integrationChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if(checkedIds.size() == 0)
            {
                mLightItemAdapter.setLights(new ArrayList());
                UIUtil.updateUIChips(mLightItemAdapter);
            }
            else if(checkedIds.size() == group.getChildCount())
            {
                LightManager.getInstance().getLights((lights, success) -> {
                    for(int i = 0; i < lights.size(); i++)
                    {
                        if(mLightItemAdapter.currSelectedlightItems.containsKey(lights.get(i).getUid()))
                        {
                            lights.get(i).setSelected(true);
                        }
                    }
                    mLightItemAdapter.setLights(lights);
                    UIUtil.updateUIChips(mLightItemAdapter);
                });
            }
            else {
                for (int id : checkedIds) {
                    List<Light> integrationlights = mLightItemAdapter.getLightsFromIntegrationMap(IntegrationType.values()[id]);
                    for(int i = 0; i < integrationlights.size(); i++)
                    {
                        if(mLightItemAdapter.currSelectedlightItems.containsKey(integrationlights.get(i).getUid()))
                        {
                            integrationlights.get(i).setSelected(true);
                        }
                    }
                    mLightItemAdapter.setLights(integrationlights);
                    UIUtil.updateUIChips(mLightItemAdapter);
                }
            }
        });

        alertDialogBuilder.setView(customDialogView)
                .setTitle("Add Room")
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = roomName.getEditText().getText().toString();
                    RoomManager.getInstance().createRoom(name, (room, status) -> {
                        Room newRoom = room;
                        List<Light> lightsToAdd = new ArrayList<>(mLightItemAdapter.currSelectedlightItems.values());
                        RoomManager.getInstance().addLightsToRoom(newRoom, lightsToAdd, (var, success) -> {
                            roomAdapter.getRoomList().add(newRoom);
                            updateUI(true);
                            dialog.dismiss();
                        });
                    });


                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    public void updateUI(boolean newRoom)
    {
        if(newRoom) {
            new Handler(Looper.getMainLooper()).post(() -> {
                UIUtil.toggleViews(roomAdapter.getRoomList().size(), emptyViewTextHeader, roomRV);
                UIUtil.toggleViews(roomAdapter.getRoomList().size(), emptyViewTextBody, roomRV);
                roomAdapter.notifyDataSetChanged();
            });
        }
        else {
            RoomRepository.getInstance().getUserDefinedRooms((rooms, success) -> {
                if(roomAdapter == null)
                    return;

                roomAdapter.setRoomList(rooms);

                UIUtil.toggleViews(rooms.size(), emptyViewTextHeader, roomRV);
                UIUtil.toggleViews(rooms.size(), emptyViewTextBody, roomRV);

                new Handler(Looper.getMainLooper()).post(() -> {

                    roomAdapter.notifyDataSetChanged();
                });
            });
        }
    }

    @Override
    public void onClick(View view, int position)
    {
        Intent roomActivity = new Intent(this.getContext(), RoomActivity.class);
        roomActivity.putExtra("room", roomAdapter.getRoomList().get(position));
        startActivityForResult(roomActivity, RequestCodes.RC_ROOM_ACTIVITY);
    }
}