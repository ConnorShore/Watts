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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.LightManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.LightState;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import util.UIMessageUtil;

public class LightAdapter extends RecyclerView.Adapter<LightAdapter.Viewholder>
{
    private final String LOG_TAG = "LightAdapter";

    private final int HUE_MAX = Integer.parseInt(WattsApplication.getResourceString(R.string.color_picker_hue_max));
    private final int SATURATION_MAX = Integer.parseInt(WattsApplication.getResourceString(R.string.color_picker_saturation_max));
    private final int BRIGHTNESS_MAX = Integer.parseInt(WattsApplication.getResourceString(R.string.color_picker_brightness_max));

    private Context context;
    public List<Light> lights;

    private AlertDialog currentColorPicker;

    private LightManager lightManager = LightManager.getInstance();

    private float[] hsv;

    private int position;

    public LightAdapter(Context context, List<Light> lights) {
        this.context = context;
        this.lights = lights;
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
        Light light = lights.get(position);

        holder.lightName.setText(light.getName());
        holder.lightTitle.setText(light.getName());
        holder.lightSwitch.setChecked(light.getLightState().isOn());

        int brightness = (int)(light.getLightState().getBrightness() * 100);
        holder.brighnessBar.setProgress(brightness);

        if(light.getLightState().isOn()) {
            float hue = light.getLightState().getHue() != null
                    ? light.getLightState().getHue() * HUE_MAX
                    : 0.0f;
            float saturation = light.getLightState().getSaturation() != null
                    ? light.getLightState().getSaturation() * SATURATION_MAX
                    : 0.0f;

            int color = Color.HSVToColor(new float[] {hue, saturation, BRIGHTNESS_MAX});
            toggleBackgroundGlow(true, holder.glowCard, color);
        } else {
            toggleBackgroundGlow(false, holder.glowCard, 0);
        }

        holder.lightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {

                lightManager.turnOnLight(light, (var, status) -> {
                    if(!status.success) {
                        Log.e(LOG_TAG, status.message);

                        UIMessageUtil.showShortToastMessage(buttonView.getContext(), "Failed to turn on light: " + light.getName());
                        return null;
                    }

                    setBackgroundGlowForLight(light, holder.glowCard);
                    UIMessageUtil.showShortToastMessage(buttonView.getContext(), "Turned on light: " + light.getName());
                    return null;
                });
            } else {
                lightManager.turnOffLight(light, (var, status) -> {
                    if(!status.success) {
                        Log.e(LOG_TAG, status.message);

                        UIMessageUtil.showShortToastMessage(buttonView.getContext(), "Failed to turn off light: " + light.getName());
                        return null;
                    }

                    setBackgroundGlowForLight(light, holder.glowCard);
                    UIMessageUtil.showShortToastMessage(buttonView.getContext(), "Turned off light: " + light.getName());
                    return null;
                });
            }
        });

        holder.lightCard.setOnLongClickListener(view -> {
            setPosition(holder.getLayoutPosition());
            return false;
        });
    }

    private void setBackgroundGlowForLight(Light light, MaterialCardView glowCard) {
        if(light.getLightState().isOn()) {
            float hue = light.getLightState().getHue() != null
                    ? light.getLightState().getHue() * HUE_MAX
                    : 0.0f;
            float saturation = light.getLightState().getSaturation() != null
                    ? light.getLightState().getSaturation() * SATURATION_MAX
                    : 0.0f;

            int color = Color.HSVToColor(new float[] {hue, saturation, BRIGHTNESS_MAX});
            toggleBackgroundGlow(true, glowCard, color);
        } else {
            toggleBackgroundGlow(false, glowCard, 0);
        }
    }

    private void toggleBackgroundGlow(boolean on, MaterialCardView glowCard, int color) {
        if(on) {
            glowCard.setCardBackgroundColor(color);
        }
        else {
            glowCard.setCardBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        return lights.size();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
        private MaterialAlertDialogBuilder alertDialogBuilder;
        private TextView lightName;
        private TextView lightTitle;
        private SwitchMaterial lightSwitch;
        private AppCompatSeekBar brighnessBar;
        private MaterialCardView lightCard;
        private ColorPickerView colorPickerView;
        private MaterialCardView glowCard;
        private View customDialogView;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            lightName = itemView.findViewById(R.id.lightName);
            lightSwitch = itemView.findViewById(R.id.lightSwitch);
            lightCard = itemView.findViewById(R.id.lightCard);
            glowCard = itemView.findViewById(R.id.lightGlowCard);

            init(itemView);

            lightCard.setOnClickListener(this);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        private void init(View v) {
            initializeColorPickerDialog(v);
            initalizeListeners();
        }

        @Override
        public void onClick(View v) {
            showColorPicker();
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(R.id.ctx_menu_group_lights, R.id.ctx_menu_item_details, Menu.NONE, "View Details");

            SpannableString s = new SpannableString("Delete");
            s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
            menu.add(R.id.ctx_menu_group_lights, R.id.ctx_menu_item_delete, Menu.NONE, s);
        }

        private void initializeColorPickerDialog(@NotNull View view) {
            customDialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.light_detail_dialog, null, false);
            alertDialogBuilder = new MaterialAlertDialogBuilder(view.getContext());

            lightTitle = customDialogView.findViewById(R.id.lightNameTitle);

            brighnessBar = customDialogView.findViewById(R.id.brightnessSlideBar);
            brighnessBar.setMax(100);
            brighnessBar.setProgress(100);

            colorPickerView = customDialogView.findViewById(R.id.colorPickerView);
            hsv = new float[3];
        }

        private void initalizeListeners() {
            colorPickerView.setColorListener((ColorListener) (color, fromUser) -> {
                ColorEnvelope colorEnvelope = new ColorEnvelope(color);
                int[] rgb = colorEnvelope.getArgb();

                Color c = new Color();
                c.RGBToHSV(rgb[1], rgb[2], rgb[3], hsv);
            });


            alertDialogBuilder.setPositiveButton("Set", (dialog, which) -> {
                onColorSet();
                dialog.dismiss();
                currentColorPicker = null;
            });
        }

        private void onColorSet() {
            float brightness = brighnessBar.getProgress() / 100.0f;
            float hue = (hsv[0]);
            float saturation = hsv[1] / SATURATION_MAX;
            toggleBackgroundGlow(true, glowCard, Color.HSVToColor(new float[] {hue, saturation, brightness}));

            Light light = lights.get(this.getAbsoluteAdapterPosition());
            LightState lightState = new LightState(true, brightness, hue / HUE_MAX, saturation);
            LightManager.getInstance().setLightState(light, lightState, (var, status) -> {
                if(!status.success) {
                    Log.e(LOG_TAG, status.message);
                    UIMessageUtil.showShortToastMessage(WattsApplication.getAppContext(), "Failed to set light state");
                    return null;
                }

                UIMessageUtil.showShortToastMessage(WattsApplication.getAppContext(), "Successfully set light state");
                return null;
            });
        }

        private void showColorPicker() {
            Light light = lights.get(this.getAbsoluteAdapterPosition());

            float hue = light.getLightState().getHue() != null ? light.getLightState().getHue() : 0.0f;
            float saturation = light.getLightState().getSaturation() != null ? light.getLightState().getSaturation() : 0.0f;
            float hsv[] = { hue * HUE_MAX, saturation * SATURATION_MAX, BRIGHTNESS_MAX};
            int color = Color.HSVToColor(hsv);

            if(currentColorPicker != null)
                currentColorPicker.dismiss();

            if(customDialogView.getParent() != null)
                ((ViewGroup)customDialogView.getParent()).removeView(customDialogView);

            alertDialogBuilder.setView(customDialogView);
            currentColorPicker = alertDialogBuilder.create();

            colorPickerView.setInitialColor(color);

            currentColorPicker.show();
        }
    }
}
