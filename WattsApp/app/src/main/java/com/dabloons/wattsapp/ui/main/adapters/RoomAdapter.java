package com.dabloons.wattsapp.ui.main.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.model.RoomModel;
import com.dabloons.wattsapp.ui.main.OnItemClickListener;

import java.util.ArrayList;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.Viewholder>
{

    private Context context;
    private ArrayList<RoomModel> mRoomModelArrayList;
    private OnItemClickListener clickListener;

    public RoomAdapter(Context context, ArrayList<RoomModel> roomModelArrayList) {
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
        RoomModel model = mRoomModelArrayList.get(position);
        holder.integrationName.setText(model.getIntegrationName());
    }

    @Override
    public int getItemCount() {
        return mRoomModelArrayList.size();
    }

    public void setClickListener(OnItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        private TextView integrationName;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            integrationName = itemView.findViewById(R.id.integrationName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            clickListener.onClick(view, getLayoutPosition()); // call the onClick in the OnItemClickListener
        }
    }
}
