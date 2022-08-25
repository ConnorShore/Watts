package com.dabloons.wattsapp.ui.main.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.manager.LightManager;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.ui.room.adapters.LightAdapter;

import java.util.ArrayList;

public class IntegrationAdapter extends RecyclerView.Adapter<IntegrationAdapter.ViewHolder>
{
    private final String LOG_TAG = "InegrationAdapter";
    private Context context;
    public ArrayList<IntegrationType> integrationTypeList;

    public IntegrationAdapter(Context context, ArrayList<IntegrationType> integrationTypeList) {
        this.context = context;
        this.integrationTypeList = integrationTypeList;
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


    }

    @Override
    public int getItemCount() {
        if(integrationTypeList == null)
        {
            return 0;
        }
        return integrationTypeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView integrationName;
        private TextView integrationConnectStatus;
        private Button deleteIntegrationButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            integrationConnectStatus = itemView.findViewById(R.id.integrationConnectedStatus);
            integrationName = itemView.findViewById(R.id.integrationName);
            deleteIntegrationButton = itemView.findViewById(R.id.delete_integration_btn);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
