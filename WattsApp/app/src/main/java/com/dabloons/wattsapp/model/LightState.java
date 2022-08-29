package com.dabloons.wattsapp.model;

public class LightState {
    public boolean on;
    public float brightness;
    public float[] hsv;
    // Will add more vals in future

    public LightState()
    {

    }
    public LightState(boolean isOn, float brightness) {
        this.on = isOn;

        // limit brightness between 0 and 1
        this.brightness = Math.min(brightness, 1.0f);
        this.brightness = Math.max(this.brightness, 0.0f);
    }

    public LightState(boolean on, float brightness, float[] hsv) {
        this.on = on;
        this.brightness = brightness;
        this.hsv = hsv;
    }

    public int getPhillipsHueBrightness() {
        return (int)(this.brightness * 254.0f);
    }
}
