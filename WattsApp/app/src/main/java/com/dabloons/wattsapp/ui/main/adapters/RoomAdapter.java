package com.dabloons.wattsapp.ui.main.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.manager.RoomManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.ui.main.OnItemClickListener;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

import util.UIMessageUtil;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.Viewholder>
{

    private final String LOG_TAG = "RoomAdapter";

    private Context context;
    private ArrayList<Room> mRoomModelArrayList;
    private OnItemClickListener clickListener;


    public RoomAdapter(Context context, ArrayList<Room> roomModelArrayList) {
        this.context = context;
        this.mRoomModelArrayList = roomModelArrayList;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position)
    {
        Room room = mRoomModelArrayList.get(position);
        holder.roomName.setText(room.getName());
        holder.roomSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                RoomManager.getInstance().turnOnRoomLights(room, (var, status) -> {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (status.success)
                            UIMessageUtil.showShortToastMessage(buttonView.getContext(), "Turned on lights for room: " + room.getName());
                        else
                            UIMessageUtil.showShortToastMessage(buttonView.getContext(), "Failed to turn on lights for room: " + room.getName());
                    });

                    return null;
                });
            }
            else
            {
                RoomManager.getInstance().turnOffRoomLights(room, (var, status) -> {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (status.success)
                            UIMessageUtil.showShortToastMessage(buttonView.getContext(), "Turned off lights for room: " + room.getName());
                        else
                            UIMessageUtil.showShortToastMessage(buttonView.getContext(), "Failed to turn off lights for room: " + room.getName());
                    });

                    return null;
                });
            }
        });
    }

    public List<Room> getRoomList() {
        return mRoomModelArrayList;
    }

    public void setRoomList(ArrayList<Room> roomList) { this.mRoomModelArrayList = roomList; }

    @Override
    public int getItemCount() {
        return mRoomModelArrayList.size();
    }

    public void setClickListener(OnItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        private TextView roomName;
        private SwitchMaterial roomSwitch;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.roomName);
            roomSwitch = itemView.findViewById(R.id.roomSwitch);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getLayoutPosition()); // call the onClick in the OnItemClickListener
        }
    }
}
