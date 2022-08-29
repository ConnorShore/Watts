package com.dabloons.wattsapp.model;

import javax.annotation.Nullable;

public class LightState {
    private boolean on;
    private float brightness;
    private Float hue;
    private Float saturation;

    public LightState(boolean isOn, float brightness, Float hue, Float saturation) {
        this.on = isOn;

        // limit brightness between 0 and 1
        this.brightness = Math.min(brightness, 1.0f);
        this.brightness = Math.max(this.brightness, 0.0f);

        this.hue = hue;
        this.saturation = saturation;
    }

    public LightState(boolean isOn, float brightness) {
        this.on = isOn;

        // limit brightness between 0 and 1
        this.brightness = Math.min(brightness, 1.0f);
        this.brightness = Math.max(this.brightness, 0.0f);

        this.hue = null;
        this.saturation = null;
    }

    public LightState()
    {

    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public Float getHue() {
        return hue;
    }

    public void setHue(float hue) {
        this.hue = hue;
    }

    public Float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }
}
