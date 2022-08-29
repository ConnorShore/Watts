package com.dabloons.wattsapp.ui.room.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.manager.IntegrationSceneManager;
import com.dabloons.wattsapp.model.Scene;
import com.dabloons.wattsapp.model.integration.IntegrationScene;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.WattsCallback;
import util.WattsCallbackStatus;

public class SceneDropdownAdapter extends RecyclerView.Adapter<SceneDropdownAdapter.Viewholder> {

    private final String LOG_TAG = "SceneDropdownAdapter";

    private Context context;
    public ArrayList<IntegrationType> integrationSceneArrayList;
    private Map<IntegrationType, IntegrationScene> selectedScenes;

    public SceneDropdownAdapter(Context context, ArrayList<IntegrationType> integrationSceneArrayList) {
        this.context = context;
        this.integrationSceneArrayList = integrationSceneArrayList;
        this.selectedScenes = new HashMap<IntegrationType, IntegrationScene>();
    }

    @NonNull
    @Override
    public SceneDropdownAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scene_dropdown_layout, parent, false);
        return new SceneDropdownAdapter.Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SceneDropdownAdapter.Viewholder holder, int position)
    {
        IntegrationType type = integrationSceneArrayList.get(position);
        IntegrationSceneManager.getInstance().getIntegrationScenes(type, (scenes, status) -> {

            holder.integrationName.setText(setIntegrationName(type));
            String[] sceneNames = getSceneNames(scenes);
            holder.integrationSceneList.setSimpleItems(sceneNames);
            holder.integrationSceneList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    setSelectedScene(scenes.get(position));
                }
            });

            return null;
        });
    }

    public void setSelectedScene(IntegrationScene scene)
    {
        selectedScenes.put(scene.getIntegrationType(), scene);
    }

    public Map<IntegrationType, IntegrationScene> getSelectedScenes() {
        return selectedScenes;
    }

    private String[] getSceneNames(List<IntegrationScene> var)
    {
        String[] ret = new String[var.size()];

        for(int i = 0; i < var.size(); i++)
        {
            ret[i] = var.get(i).getName();
        }

        return ret;
    }

    private String setIntegrationName(IntegrationType type)
    {
        switch (type){
            case PHILLIPS_HUE:
                return "Phillips Hue";
            case NANOLEAF:
                return "Nanoleaf";
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return integrationSceneArrayList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder implements AdapterView.OnClickListener {
        TextView integrationName;
        MaterialAutoCompleteTextView integrationSceneList;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            integrationName = itemView.findViewById(R.id.dropDownIntegrationName);
            integrationSceneList = itemView.findViewById(R.id.integrationSceneMenuTextView);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
