package com.dabloons.wattsapp.ui.room.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.manager.IntegrationSceneManager;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.integration.IntegrationAuth;
import com.dabloons.wattsapp.model.integration.IntegrationScene;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.model.integration.NanoleafPanelAuthCollection;
import com.dabloons.wattsapp.model.integration.NanoleafPanelIntegrationAuth;
import com.dabloons.wattsapp.service.NanoleafService;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class SceneDropdownAdapter extends RecyclerView.Adapter<SceneDropdownAdapter.Viewholder> {

    private final String LOG_TAG = "SceneDropdownAdapter";

    private Context context;
    public List<IntegrationAuth> integrationAuths;
    private Map<IntegrationAuth, IntegrationScene> selectedScenes;

    public SceneDropdownAdapter(Context context, ArrayList<IntegrationAuth> integrationAuths) {
        this.context = context;
        this.integrationAuths = integrationAuths;
        this.selectedScenes = new HashMap<IntegrationAuth, IntegrationScene>();
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
        IntegrationType type = integrationAuths.get(position).getIntegrationType();

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
        UserManager.getInstance().getIntegrationAuthData(scene.getIntegrationType(), new WattsCallback<IntegrationAuth, Void>() {
            @Override
            public Void apply(IntegrationAuth integrationAuth, WattsCallbackStatus status)
            {
                selectedScenes.put(integrationAuth, scene);
                return null;
            }
        });

    }

    public Map<IntegrationAuth, IntegrationScene> getSelectedScenes() {
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
