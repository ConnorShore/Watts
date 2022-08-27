package com.dabloons.wattsapp.ui.room.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.model.Light;

import java.util.ArrayList;

public class LightItemAdapter extends RecyclerView.Adapter<LightItemAdapter.Viewholder>{

    private final String LOG_TAG = "LightItemAdapter";

    private Context context;
    public ArrayList<Light> lightModelArrayList;

    public LightItemAdapter(Context context, ArrayList<Light> lightList) {
        this.context = context;
        this.lightModelArrayList = lightList;
    }

    @NonNull
    @Override
    public LightItemAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.light_list_layout, parent, false);
        return new LightItemAdapter.Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LightItemAdapter.Viewholder holder, int position) {
        Light model = lightModelArrayList.get(position);
        holder.lightName.setText(model.getName());

        holder.itemView.setBackgroundColor(model.isSelected() ? this.context.getColor(R.color.md_theme_dark_onPrimary) : this.context.getColor(R.color.md_theme_dark_surfaceVariant));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.setSelected(!model.isSelected());
                holder.itemView.setBackgroundColor(model.isSelected() ? WattsApplication.getAppContext().getColor(R.color.md_theme_dark_onPrimary) : WattsApplication.getAppContext().getColor(R.color.md_theme_dark_surfaceVariant));
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
