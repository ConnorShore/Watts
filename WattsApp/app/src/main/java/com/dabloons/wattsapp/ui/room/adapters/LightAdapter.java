package com.dabloons.wattsapp.ui.room.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.manager.LightManager;
import com.dabloons.wattsapp.model.Light;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import util.UIMessageUtil;

public class LightAdapter extends RecyclerView.Adapter<LightAdapter.Viewholder>
{
    private final String LOG_TAG = "LightAdapter";

    private Context context;
    public ArrayList<Light> lightModelArrayList;

    public LightAdapter(Context context, ArrayList<Light> lightModelArrayList) {
        this.context = context;
        this.lightModelArrayList = lightModelArrayList;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.light_card_layout, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position)
    {
        Light light = lightModelArrayList.get(position);
        holder.lightName.setText(light.getName());
        holder.lightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                LightManager.getInstance().turnOnLight(light, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e)
                    {
//
                        Log.e(LOG_TAG, e.getMessage());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
                    {
                        if(response.isSuccessful())
                        {
                            UIMessageUtil.showShortToastMessage(buttonView.getContext(), "Turned on light: " + light.getName());
                        }
                        else
                        {
                            UIMessageUtil.showShortToastMessage(buttonView.getContext(), "Failed to turn on light: " + light.getName());
                        }
                    }
                });
            }
            else
            {
                LightManager.getInstance().turnOffLight(light, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e)
                    {
                        UIMessageUtil.showShortToastMessage(buttonView.getContext(), "Failed to turn off light: " + light.getName());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
                    {
                        if(response.isSuccessful())
                        {
                            UIMessageUtil.showShortToastMessage(buttonView.getContext(), "Turned off light: " + light.getName());
                        }
                        else
                        {
                            UIMessageUtil.showShortToastMessage(buttonView.getContext(), "Failed to turn off light: " + light.getName());
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return lightModelArrayList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        private TextView lightName;
        private SwitchMaterial lightSwitch;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            lightName = itemView.findViewById(R.id.lightName);
            lightSwitch = itemView.findViewById(R.id.lightSwitch);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
