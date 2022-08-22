package com.dabloons.wattsapp.ui.main.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.ui.main.OnItemClickListener;
import com.dabloons.wattsapp.ui.main.adapters.RoomAdapter;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements OnItemClickListener {

    private RecyclerView roomRV;

    // Arraylist for storing data
    private ArrayList<Room> roomModelList;

    public HomeFragment() {

        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        roomRV = v.findViewById(R.id.idRVCourse);

        // here we have created new array list and added data to it.
        roomModelList = new ArrayList<>();
//        roomModelList.add(new Room("Living Room"));
//        roomModelList.add(new Room("Bed Room"));
//        roomModelList.add(new Room("Kitchen"));
//        roomModelList.add(new Room("Living Room"));
//        roomModelList.add(new Room("Bed Room"));
//        roomModelList.add(new Room("Kitchen"));
//        roomModelList.add(new Room("Living Room"));
//        roomModelList.add(new Room("Bed Room"));
//        roomModelList.add(new Room("Kitchen"));


        // we are initializing our adapter class and passing our arraylist to it.
         RoomAdapter roomAdapter = new RoomAdapter(this.getContext(), roomModelList);

        // below line is for setting a layout manager for our recycler view.
        // here we are creating vertical list so we will provide orientation as vertical
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);

        // in below two lines we are setting layoutmanager and adapter to our recycler view.
        roomRV.setLayoutManager(linearLayoutManager);
        roomRV.setAdapter(roomAdapter);
        roomAdapter.setClickListener(this);

        return  v;
    }


    @Override
    public void onClick(View view, int position) {
        System.out.println("");
    }
}