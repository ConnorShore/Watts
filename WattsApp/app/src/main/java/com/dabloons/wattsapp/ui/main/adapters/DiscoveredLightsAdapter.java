package com.dabloons.wattsapp.ui.main.adapters;

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
import com.dabloons.wattsapp.model.integration.NanoleafPanelIntegrationAuth;
import com.dabloons.wattsapp.ui.room.adapters.LightItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class DiscoveredLightsAdapter extends RecyclerView.Adapter<DiscoveredLightsAdapter.ViewHolder> {

    private final String LOG_TAG = "DiscoveredLightsAdapter";

    private Context context;
    public List<NanoleafPanelIntegrationAuth> lights;

    public DiscoveredLightsAdapter(Context context, List<NanoleafPanelIntegrationAuth> lights) {
        this.context = context;
        this.lights = lights;
    }

    @Override
    public DiscoveredLightsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.discovered_lights_layout, parent, false);
        return new DiscoveredLightsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscoveredLightsAdapter.ViewHolder holder, int position) {
        NanoleafPanelIntegrationAuth light = lights.get(position);

        holder.lightName.setText(light.getName());
        holder.integration.setText("Nanoleaf");
        holder.itemView.setBackgroundColor(light.isSelected() ? this.context.getColor(R.color.md_theme_dark_onPrimary) : this.context.getColor(R.color.md_theme_dark_surfaceVariant));
        holder.itemView.setOnClickListener(v -> {
            light.setSelected(!light.isSelected());
            holder.itemView.setBackgroundColor(light.isSelected() ? WattsApplication.getAppContext().getColor(R.color.md_theme_dark_onPrimary) : WattsApplication.getAppContext().getColor(R.color.md_theme_dark_surfaceVariant));
        });
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        lights = new ArrayList<>();
    }

    @Override
    public int getItemCount() { return lights.size(); }

    public void setLights(List<NanoleafPanelIntegrationAuth> lights) {
        this.lights = lights;
    }

    public List<NanoleafPanelIntegrationAuth> getSelectedLights() {
        List<NanoleafPanelIntegrationAuth> ret = new ArrayList<>();
        for(NanoleafPanelIntegrationAuth panel : lights) {
            if(panel.isSelected())
                ret.add(panel);
        }
        return ret;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView lightName;
        private TextView integration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lightName = itemView.findViewById(R.id.discoveredLightName);
            integration = itemView.findViewById(R.id.discoveredLightIntegration);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
