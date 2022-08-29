package com.dabloons.wattsapp.ui.room.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
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
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import util.UIMessageUtil;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class SceneAdapter extends RecyclerView.Adapter<SceneAdapter.Viewholder>
{

    private final String LOG_TAG = "SceneAdapter";

    private Context context;
    public List<Scene> scenes;
    private int position;

    private SceneManager sceneManager = SceneManager.getInstance();

    public SceneAdapter(Context context, List<Scene> scenes) {
        this.context = context;
        this.scenes = scenes;
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
        Scene scene = scenes.get(position);
        holder.sceneName.setText(scene.getName());
        holder.setScene.setOnClickListener(view -> {
            sceneManager.activateScene(scene, (var, status) -> {
                if(!status.success) {
                    Log.e(LOG_TAG, status.message);
                    UIMessageUtil.showShortToastMessage(context, "Failed to activate scene " + scene.getName());
                    return null;
                }

                UIMessageUtil.showShortToastMessage(context, "Activated scene " + scene.getName());
                return null;
            });
        });
        holder.sceneCard.setOnLongClickListener(v -> {
            setPosition(holder.getLayoutPosition());
            return false;
        });
    }

    @Override
    public int getItemCount() {

        if(scenes == null)
            return 0;
        return scenes.size();
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
        private TextView sceneName;
        private Button setScene;
        private MaterialCardView sceneCard;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            sceneName = itemView.findViewById(R.id.sceneName);
            setScene = itemView.findViewById(R.id.set_scene);
            sceneCard = itemView.findViewById(R.id.sceneCard);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {

        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(R.id.ctx_menu_group_scenes, R.id.ctx_menu_item_details, Menu.NONE, "View Details");

            SpannableString s = new SpannableString("Delete");
            s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
            menu.add(R.id.ctx_menu_group_scenes, R.id.ctx_menu_item_delete, Menu.NONE, s);
        }
    }
}
