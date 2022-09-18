package com.dabloons.wattsapp.ui.main.adapters;

import android.content.Context;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.LightManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.ui.room.adapters.LightAdapter;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.WattsCallback;
import util.WattsCallbackStatus;

public class IntegrationAdapter extends RecyclerView.Adapter<IntegrationAdapter.ViewHolder>
{
    private final String LOG_TAG = "IntegrationAdapter";
    private Context context;
    public ArrayList<IntegrationType> integrationTypeList;

    private Map<IntegrationType, Boolean> expandedList;

    private IntegrationType previousExpanded = IntegrationType.NONE;

    public IntegrationAdapter(Context context, ArrayList<IntegrationType> integrationTypeList) {
        this.context = context;
        this.integrationTypeList = integrationTypeList;
        expandedList = new HashMap<>();
        for(IntegrationType type : integrationTypeList) {
            expandedList.put(type, false);
        }
    }

    @NonNull
    @Override
    public IntegrationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.integration_card_layout, parent, false);
        return new IntegrationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IntegrationAdapter.ViewHolder holder, int position) {
        IntegrationType type = integrationTypeList.get(position);
        holder.integrationConnectStatus.setText("Connected");
        holder.integrationConnectStatus.setTextColor(this.context.getColor(R.color.connected));

        holder.numLightsText.setText(String.format(WattsApplication.getResourceString(R.string.number_lights_integration), 0));

        LightManager.getInstance().getLightsForIntegration(type, (lights, status) -> {
            String baseTxt = WattsApplication.getResourceString(R.string.number_lights_integration);
            String finalTxt = String.format(baseTxt, lights.size());
            holder.numLightsText.setText(finalTxt);
        });

        boolean expanded = expandedList.get(type);
        if(expanded) {
            holder.hiddenView.setVisibility(View.VISIBLE);
            holder.selectableCard.setStrokeColor(WattsApplication.getColorInt(R.color.app_orange_primary));
        }
        else {
            holder.hiddenView.setVisibility(View.GONE);
            holder.selectableCard.setStrokeColor(WattsApplication.getColorInt(R.color.app_light_grey));
        }

        switch(type){
            case PHILLIPS_HUE:
                holder.integrationName.setText("Phillips Hue");
                break;
            case NANOLEAF:
                holder.integrationName.setText("Nanoleaf");
                break;
            default:
                Log.w(LOG_TAG, "Integration: "+type+ " is not defined.");
        }

        holder.selectableCard.setOnClickListener(v -> {
            if(!expanded) {
                if(previousExpanded != IntegrationType.NONE) {
                    expandedList.put(previousExpanded, false);
                    notifyItemChanged(integrationTypeList.indexOf(previousExpanded));
                }

                previousExpanded = type;
            }

            TransitionManager.beginDelayedTransition((MaterialCardView)v, new AutoTransition());
            expandedList.put(type, !expanded);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        if(integrationTypeList == null)
        {
            return 0;
        }
        return integrationTypeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView integrationName;
        private TextView integrationConnectStatus;
        private TextView numLightsText;
        private Button deleteIntegrationButton;
        private MaterialCardView selectableCard;
        private LinearLayout hiddenView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            integrationConnectStatus = itemView.findViewById(R.id.integrationConnectedStatus);
            integrationName = itemView.findViewById(R.id.integrationName);
            deleteIntegrationButton = itemView.findViewById(R.id.delete_integration_button);
            hiddenView = itemView.findViewById(R.id.hidden_view);
            selectableCard = itemView.findViewById(R.id.integrationSelectableCard);
            numLightsText = itemView.findViewById(R.id.numLightsText);
        }
    }
}
