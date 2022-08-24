package com.dabloons.wattsapp.ui.main.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.ui.main.OnItemClickListener;

import java.util.ArrayList;

public class LightAdapter extends RecyclerView.Adapter<LightAdapter.Viewholder>{

    private final String LOG_TAG = "LightAdapter";

    private Context context;
    private ArrayList<Light> lightModelArrayList;
    private OnItemClickListener clickListener;

    public LightAdapter(Context context, ArrayList<Light> lightList) {
        this.context = context;
        this.lightModelArrayList = lightList;
    }

    @NonNull
    @Override
    public LightAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new LightAdapter.Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LightAdapter.Viewholder holder, int position) {
        Light model = lightModelArrayList.get(position);
        holder.lightName.setText(model.getName());
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        private TextView lightName;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            lightName = itemView.findViewById(R.id.roomName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

//            clickListener.onClick(view, getLayoutPosition()); // call the onClick in the OnItemClickListener
        }
    }
}
