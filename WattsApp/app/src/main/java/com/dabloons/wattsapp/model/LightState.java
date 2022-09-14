package com.dabloons.wattsapp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class LightState implements Parcelable {
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
        this.on = false;
        this.brightness = 0;
        this.hue = null;
        this.saturation = null;
    }

    protected LightState(Parcel in) {
        on = in.readByte() != 0;
        brightness = in.readFloat();
        if (in.readByte() == 0) {
            hue = null;
        } else {
            hue = in.readFloat();
        }
        if (in.readByte() == 0) {
            saturation = null;
        } else {
            saturation = in.readFloat();
        }
    }

    public static final Creator<LightState> CREATOR = new Creator<LightState>() {
        @Override
        public LightState createFromParcel(Parcel in) {
            return new LightState(in);
        }

        @Override
        public LightState[] newArray(int size) {
            return new LightState[size];
        }
    };

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

    public void setHue(Float hue) {
        this.hue = hue;
    }

    public Float getSaturation() {
        return saturation;
    }

    public void setSaturation(Float saturation) {
        this.saturation = saturation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (on ? 1 : 0));
        dest.writeFloat(brightness);
        if (hue == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeFloat(hue);
        }
        if (saturation == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeFloat(saturation);
        }
    }
}
