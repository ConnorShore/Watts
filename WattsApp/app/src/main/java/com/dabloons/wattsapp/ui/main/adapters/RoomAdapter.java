package com.dabloons.wattsapp.ui.main.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.manager.LightManager;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.RoomManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.LightState;
import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.ui.main.OnItemClickListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import util.UIMessageUtil;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.Viewholder>
{

    private final String LOG_TAG = "RoomAdapter";

    private Context context;
    private ArrayList<Room> mRoomModelArrayList;
    private OnItemClickListener clickListener;

    private final RoomManager roomManager = RoomManager.getInstance();
    private final LightManager lightManager = LightManager.getInstance();

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

    private void toggleBackgroundGlow(boolean on, MaterialCardView glowCard, int color) {
        if(on) {
            glowCard.setCardBackgroundColor(color);
        }
        else {
            glowCard.setCardBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position)
    {
        Room room = mRoomModelArrayList.get(position);
        holder.roomName.setText(room.getName());
        holder.glowCard.setCardBackgroundColor(Color.TRANSPARENT);
        lightManager.getLightsForIds(room.getLightIds(), (lights, status) -> {
            boolean on = isRoomLightsOn(lights);

            if(on)
                holder.roomSwitch.setChecked(true);
            else
                holder.roomSwitch.setChecked(false);

            setSwitchOnClickListener(holder, room);

            if(on)
                toggleBackgroundGlow(true, holder.glowCard, getAverageColorOfLights(lights));
        });
    }

    private void setSwitchOnClickListener(@NonNull Viewholder holder, Room room) {
        holder.roomSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                lightManager.getLightsForIds(room.getLightIds(), (lights, status) -> {
                    if(!status.success) {
                        UIMessageUtil.showShortToastMessage(buttonView.getContext(), "Failed to turn on lights for room: " + room.getName());
                        return;
                    }

                    int color = getAverageColorOfLights(lights); // todo: set to average color of all lights that will be on
                    toggleBackgroundGlow(true, holder.glowCard, color);

                    roomManager.turnOnRoomLights(room, (var, status1) -> {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (status1.success)
                                UIMessageUtil.showShortToastMessage(buttonView.getContext(), "Turned on lights for room: " + room.getName());
                            else
                                UIMessageUtil.showShortToastMessage(buttonView.getContext(), "Failed to turn on lights for room: " + room.getName());

                        });
                    });
                });
            }
            else
            {
                toggleBackgroundGlow(false, holder.glowCard, Color.TRANSPARENT);
                RoomManager.getInstance().turnOffRoomLights(room, (var, status) -> {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (status.success)
                            UIMessageUtil.showShortToastMessage(buttonView.getContext(), "Turned off lights for room: " + room.getName());
                        else
                            UIMessageUtil.showShortToastMessage(buttonView.getContext(), "Failed to turn off lights for room: " + room.getName());
                    });
                });
            }
        });
    }

    public List<Room> getRoomList() {
        return mRoomModelArrayList;
    }

    public void setRoomList(ArrayList<Room> roomList) { this.mRoomModelArrayList = roomList; }

    private int getAverageColorOfLights(List<Light> lights) {
        float averageHue = 0.0f;
        float averageSaturation = 0.0f;
        int numCounted = 0;
        for(Light light : lights) {
            LightState state = light.getLightState();
            if(state.getHue() != null && state.getSaturation() != null) {
                if(light.getIntegrationType() == IntegrationType.NANOLEAF
                    && state.getHue() == 0
                    && state.getSaturation() == 0)
                    continue;
                averageHue += state.getHue();
                averageSaturation += state.getSaturation();
                numCounted++;
            }
        }

        averageHue /= numCounted;
        averageSaturation /= numCounted;

        averageHue *= 360.0f;

        float hsv[] = {averageHue, averageSaturation, 1.0f};
        return Color.HSVToColor(hsv);
    }

    private boolean isRoomLightsOn(List<Light> roomLights) {
        for(Light l : roomLights)
            if(l.getLightState().isOn())
                return true;
        return false;
    }

    @Override
    public int getItemCount() {
        if(mRoomModelArrayList == null)
            return 0;

        return mRoomModelArrayList.size();
    }

    public void setClickListener(OnItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        private TextView roomName;
        private SwitchMaterial roomSwitch;
        private MaterialCardView selectableCard;
        private MaterialCardView glowCard;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.roomName);
            roomSwitch = itemView.findViewById(R.id.roomSwitch);
            selectableCard = itemView.findViewById(R.id.roomSelectableCard);
            glowCard = itemView.findViewById(R.id.roomGlowCard);

            selectableCard.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getLayoutPosition()); // call the onClick in the OnItemClickListener
        }
    }
}
