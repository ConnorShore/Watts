package com.dabloons.wattsapp.ui.room.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.SceneManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.Scene;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import util.UIMessageUtil;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class SceneAdapter extends RecyclerView.Adapter<SceneAdapter.Viewholder>
{
    private final String LOG_TAG = "SceneAdapter";

    private Context context;
    public List<Scene> scenes;
    private int position;
    private Activity currentActivity;

    private SceneManager sceneManager = SceneManager.getInstance();

    private SceneAdapter.Viewholder previousToggledHolder = null;
    private Scene previousToggledScene = null;

    public SceneAdapter(Context context, Activity activity, List<Scene> scenes) {
        this.context = context;
        this.scenes = scenes;
        this.currentActivity = activity;
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
        setPreviewBackgroundColor(holder.scenePreview, scene.getSceneColorsArray());

        if(scene.isOn()) {
            toggleSceneCardOn(holder, scene);
            previousToggledHolder = holder;
            previousToggledScene = scene;
        }
        else
            toggleSceneCardOff(holder, scene);

        holder.sceneName.setText(scene.getName());
        holder.scenePreview.setOnClickListener(view -> {
            if(previousToggledScene != null) {
                toggleSceneOff(previousToggledHolder, previousToggledScene);
            }
            toggleSceneOn(holder, scene);
            previousToggledScene = scene;
            previousToggledHolder = holder;
        });
        holder.sceneCard.setOnLongClickListener(v -> {
            setPosition(holder.getLayoutPosition());
            return false;
        });
    }

    private void toggleSceneOn(@NonNull SceneAdapter.Viewholder holder, Scene scene) {
        sceneManager.activateScene(scene, (var, status) -> {
            if(!status.success) {
                Log.e(LOG_TAG, status.message);
                UIMessageUtil.showShortToastMessage(context, "Failed to activate scene " + scene.getName());
                return;
            }

            UIMessageUtil.showShortToastMessage(context, "Activated scene " + scene.getName());
            return;
        });

        toggleSceneCardOn(holder, scene);
    }

    private void toggleSceneOff(@NonNull SceneAdapter.Viewholder holder, Scene scene) {
        toggleSceneCardOff(holder, scene);
        sceneManager.deactivateScene(scene, (var, status) -> {});
    }

    private void toggleSceneCardOn(@NonNull SceneAdapter.Viewholder holder, Scene scene) {
        this.currentActivity.runOnUiThread(() -> {
            holder.sceneName.setTextColor(Color.WHITE);
            setCanvasBackgroundColor(holder.sceneCardCanvas, scene.getSceneColorsArray(), true);
            setPreviewBackgroundColor(holder.scenePreview, null);
        });
    }

    private void toggleSceneCardOff(@NonNull SceneAdapter.Viewholder holder, Scene scene) {
        this.currentActivity.runOnUiThread(() -> {
            holder.sceneName.setTextColor(WattsApplication.getColorInt(R.color.app_light_grey));
            setCanvasBackgroundColor(holder.sceneCardCanvas, scene.getSceneColorsArray(), false);
            setPreviewBackgroundColor(holder.scenePreview, scene.getSceneColorsArray());
        });
    }

    private void setPreviewBackgroundColor(LinearLayout card, int[] colors) {
        ImageView img = card.findViewById(R.id.sceneOnIcon);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(20.0f);

        if(colors == null || colors.length == 0) {
            img.setImageResource(R.drawable.ic_baseline_flare_24);
            drawable.setColor(Color.TRANSPARENT);
            card.setBackground(drawable);
            return;
        }

        if(colors.length == 1) {
            drawable.setColor(colors[0]);
        }
        else {
            drawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
            drawable.setColors(colors);
        }

        img.setImageResource(R.drawable.ic_power_settings_new_30px);
        card.setBackground(drawable);
    }

    private void setCanvasBackgroundColor(LinearLayout card, int[] colors, boolean on) {
        GradientDrawable drawable = new GradientDrawable();

        if(colors == null || colors.length == 0) {
            drawable.setColor(Color.TRANSPARENT);
            card.setBackground(drawable);
            return;
        }

        if(colors.length == 1) {
            drawable.setColor(colors[0]);
        }
        else {
            drawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
            drawable.setColors(colors);
        }

        if(on) drawable.setAlpha(255);
        else drawable.setAlpha(100);

        card.setBackground(drawable);
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
//        private Button setScene;
        private MaterialCardView sceneCard;
        private LinearLayout sceneCardCanvas;
        private LinearLayout scenePreview;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            sceneName = itemView.findViewById(R.id.sceneName);
//            setScene = itemView.findViewById(R.id.set_scene);
            sceneCard = itemView.findViewById(R.id.sceneCard);
            sceneCardCanvas = itemView.findViewById(R.id.sceneCardCanvas);
            scenePreview = itemView.findViewById(R.id.scenePreview);
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
