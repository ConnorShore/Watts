package com.dabloons.wattsapp.ui.main.adapters;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.model.Light;

public class LightsAdapter extends RecyclerView.Adapter<LightsAdaper.ViewHolder> {




    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView lightName;
        private TextView integration;

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
