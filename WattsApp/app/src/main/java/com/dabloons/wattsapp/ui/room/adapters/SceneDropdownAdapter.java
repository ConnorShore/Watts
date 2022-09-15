package com.dabloons.wattsapp.ui.room.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.integration.IntegrationAuth;
import com.dabloons.wattsapp.model.integration.IntegrationScene;
import com.dabloons.wattsapp.model.integration.NanoleafPanelIntegrationAuth;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SceneDropdownAdapter extends RecyclerView.Adapter<SceneDropdownAdapter.Viewholder> {

    private final String LOG_TAG = "SceneDropdownAdapter";

    public List<IntegrationAuth> integrationAuths;
    private Map<IntegrationAuth, IntegrationScene> selectedScenes;
    private Map<IntegrationAuth, List<IntegrationScene>> sceneMap;

    public SceneDropdownAdapter(Map<IntegrationAuth, List<IntegrationScene>> integrationSceneMap) {
        this.sceneMap = integrationSceneMap;
        this.integrationAuths = new ArrayList<>(integrationSceneMap.keySet());
        this.selectedScenes = new HashMap<>();
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
        IntegrationAuth auth = integrationAuths.get(position);

        List<IntegrationScene> scenesToShow = sceneMap.get(auth);
        holder.integrationName.setText(setIntegrationName(auth));

        String[] sceneNames = getSceneNames(scenesToShow);
        holder.integrationSceneList.setSimpleItems(sceneNames);
        holder.integrationSceneList.setOnItemClickListener((parent, view, position1, id) -> {
            setSelectedScene(scenesToShow.get(position1));
        });
    }

    public void setSelectedScene(IntegrationScene scene)
    {
        UserManager.getInstance().getIntegrationAuthData(scene.getIntegrationType(), (integrationAuth, status) -> {
            selectedScenes.put(integrationAuth, scene);
        });
    }

    public Map<IntegrationAuth, IntegrationScene> getSelectedScenes() {
        return selectedScenes;
    }

    private String[] getSceneNames(List<IntegrationScene> var) {
        String[] ret = new String[var.size()];

        for(int i = 0; i < var.size(); i++) {
            ret[i] = var.get(i).getName();
        }

        return ret;
    }

    private String setIntegrationName(IntegrationAuth auth) {
        switch (auth.getIntegrationType()){
            case PHILLIPS_HUE:
                return "Phillips Hue";
            case NANOLEAF:
                NanoleafPanelIntegrationAuth panel = (NanoleafPanelIntegrationAuth) auth;
                return "Nanoleaf [" + panel.getName() + "]";
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return integrationAuths.size();
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
