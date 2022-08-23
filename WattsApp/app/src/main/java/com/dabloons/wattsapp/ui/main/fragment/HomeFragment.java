package com.dabloons.wattsapp.ui.main.fragment;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.RoomManager;
import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.repository.RoomRepository;
import com.dabloons.wattsapp.ui.main.OnItemClickListener;
import com.dabloons.wattsapp.ui.main.adapters.RoomAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import util.WattsCallback;
import util.WattsCallbackStatus;

public class HomeFragment extends Fragment implements OnItemClickListener {

    private final String LOG_TAG = "HomeFragment";

    private MaterialAlertDialogBuilder alertDialogBuilder;
    private View customDialogView;

    private RecyclerView roomRV;

    // Arraylist for storing data
    private ArrayList<Room> roomModelList;
    private RoomAdapter roomAdapter;

    public HomeFragment() {

        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
             RoomRepository.getInstance().getUserDefinedRooms((rooms, success) -> {
                roomModelList = rooms;
                roomAdapter = new RoomAdapter(WattsApplication.getAppContext(), rooms);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(WattsApplication.getAppContext(), LinearLayoutManager.VERTICAL, false);

                // in below two lines we are setting layoutmanager and adapter to our recycler view.
                roomRV.setLayoutManager(linearLayoutManager);
                roomRV.setAdapter(roomAdapter);
                roomAdapter.setClickListener(this);
            return null;
        });



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        roomRV = v.findViewById(R.id.idRVCourse);

        // here we have created new array list and added data to it.

        // we are initializing our adapter class and passing our arraylist to it.


        // below line is for setting a layout manager for our recycler view.
        // here we are creating vertical list so we will provide orientation as vertical


        alertDialogBuilder = new MaterialAlertDialogBuilder(this.getContext());
        Button addRoom = (Button) v.findViewById(R.id.addRoomButton);
        addRoom.setOnClickListener(view -> {
            customDialogView = LayoutInflater.from(this.getContext()).inflate(R.layout.add_room_dialog, null, false);

            launchCustomAlertDialog();
        });
        return  v;
    }

    private void launchCustomAlertDialog()
    {
        TextInputLayout roomName = customDialogView.findViewById(R.id.roomNameTextLayout);

        alertDialogBuilder.setView(customDialogView)
                .setTitle("Add Room")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = roomName.getEditText().getText().toString();
                        Room newRoom = RoomManager.getInstance().createRoom(name);
                        roomModelList.add(newRoom);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    @Override
    public void onClick(View view, int position) {
        System.out.println("");
    }
}