package com.dabloons.wattsapp.model.integration;

import android.os.Parcel;
import android.os.Parcelable;

public class IntegrationScene implements Parcelable {


    protected IntegrationScene(Parcel in) {
    }

    public static final Creator<IntegrationScene> CREATOR = new Creator<IntegrationScene>() {
        @Override
        public IntegrationScene createFromParcel(Parcel in) {
            return new IntegrationScene(in);
        }

        @Override
        public IntegrationScene[] newArray(int size) {
            return new IntegrationScene[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
