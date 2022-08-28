package com.dabloons.wattsapp.ui.room.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.manager.SceneManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.Scene;

import java.util.ArrayList;

public class SceneAdapter extends RecyclerView.Adapter<SceneAdapter.Viewholder>
{

    private final String LOG_TAG = "SceneAdapter";

    private Context context;
    public ArrayList<Scene> sceneArrayList;

    private SceneManager sceneManager = SceneManager.getInstance();

    public SceneAdapter(Context context, ArrayList<Scene> sceneArrayList) {
        this.context = context;
        this.sceneArrayList = sceneArrayList;
    }

    @NonNull
    @Override
    public SceneAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scene_card_layout, parent, false);
        return new SceneAdapter.Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SceneAdapter.Viewholder holder, int position)
    {
        Scene scene = sceneArrayList.get(position);
        holder.sceneName.setText(scene.getName());
    }

    @Override
    public int getItemCount() {

        if(sceneArrayList == null)
            return 0;
        return sceneArrayList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView sceneName;
        private Button setScene;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            sceneName = itemView.findViewById(R.id.sceneName);
            setScene = itemView.findViewById(R.id.set_scene);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
