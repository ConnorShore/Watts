package com.dabloons.wattsapp.ui.main.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.model.Light;

import java.util.ArrayList;

public class LightAdapter extends RecyclerView.Adapter<LightAdapter.Viewholder>{

    private final String LOG_TAG = "LightAdapter";

    private Context context;
    public ArrayList<Light> lightModelArrayList;

    public LightAdapter(Context context, ArrayList<Light> lightList) {
        this.context = context;
        this.lightModelArrayList = lightList;
    }

    @NonNull
    @Override
    public LightAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout, parent, false);
        return new LightAdapter.Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LightAdapter.Viewholder holder, int position) {
        Light model = lightModelArrayList.get(position);
        holder.lightName.setText(model.getName());

        holder.itemView.setBackgroundColor(model.isSelected() ? Color.CYAN : Color.WHITE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.setSelected(!model.isSelected());
                holder.itemView.setBackgroundColor(model.isSelected() ? Color.CYAN : Color.WHITE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lightModelArrayList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        private TextView lightName;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            lightName = itemView.findViewById(R.id.lightName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
