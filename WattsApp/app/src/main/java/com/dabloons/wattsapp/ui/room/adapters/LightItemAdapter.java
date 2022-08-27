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
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.model.integration.NanoleafPanelIntegrationAuth;

import java.util.ArrayList;
import java.util.List;

public class LightItemAdapter<T> extends RecyclerView.Adapter<LightItemAdapter.Viewholder>{

    private final String LOG_TAG = "LightItemAdapter";

    private Context context;
    public List<T> lightItems;

    public LightItemAdapter(Context context, List<T> lightList) {
        this.context = context;
        this.lightItems = lightList;
    }

    @NonNull
    @Override
    public LightItemAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.light_list_layout, parent, false);
        TextView lightIcon = view.findViewById(R.id.lightListIcon);
        lightIcon.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_light_bulb_32, 0, 0, 0);
        return new LightItemAdapter.Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LightItemAdapter.Viewholder holder, int position) {
        T model = lightItems.get(position);
        String integration = "";
        String name = "";
        boolean selected = false;
        if(model instanceof Light) {
            Light l = (Light)model;
            name = l.getName();
            integration = getIntegrationString(l.getIntegrationType());
            selected = l.isSelected();
        }
        else if(model instanceof NanoleafPanelIntegrationAuth) {
            NanoleafPanelIntegrationAuth l = (NanoleafPanelIntegrationAuth)model;
            name = l.getName();
            integration = getIntegrationString(l.getIntegrationType());
            selected = l.isSelected();
        }

        holder.lightName.setText(name);
        holder.lightIntegration.setText(integration);
        holder.itemView.setBackgroundColor(selected ? this.context.getColor(R.color.md_theme_dark_onPrimary) : this.context.getColor(R.color.md_theme_dark_surfaceVariant));
        boolean passSelected = selected;// need final value for the callback
        holder.itemView.setOnClickListener(v -> {
            boolean finalSelected = false;
            if(model instanceof Light) {
                Light l = (Light) model;
                finalSelected = !((Light) model).isSelected();
                l.setSelected(finalSelected);
            }
            else if(model instanceof NanoleafPanelIntegrationAuth) {
                NanoleafPanelIntegrationAuth l = (NanoleafPanelIntegrationAuth)model;
                finalSelected = !((NanoleafPanelIntegrationAuth) model).isSelected();
                l.setSelected(!finalSelected);
            }
            holder.itemView.setBackgroundColor(finalSelected ? WattsApplication.getAppContext().getColor(R.color.md_theme_dark_onPrimary) : WattsApplication.getAppContext().getColor(R.color.md_theme_dark_surfaceVariant));
        });
    }

    @Override
    public int getItemCount() {
        return lightItems.size();
    }

    public void setLights(List<T> lights) {
        this.lightItems = lights;
    }

    public List<T> getSelectedLights() {
        List<T> ret = new ArrayList<>();
        for(T panel : lightItems) {
            if(panel instanceof Light) {
                if(((Light)panel).isSelected())
                    ret.add(panel);
            }
            else if(panel instanceof NanoleafPanelIntegrationAuth) {
                if(((NanoleafPanelIntegrationAuth)panel).isSelected())
                    ret.add(panel);
            }
        }
        return ret;
    }

    private String getIntegrationString(IntegrationType type) {
        switch(type) {
            case PHILLIPS_HUE:
                return "Phillips Hue";
            case NANOLEAF:
                return "NanoLeaf";
            default:
                return "None";
        }
    }

    public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        private TextView lightName;
        private TextView lightIntegration;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            lightName = itemView.findViewById(R.id.lightListName);
            lightIntegration = itemView.findViewById(R.id.listListIntegration);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
