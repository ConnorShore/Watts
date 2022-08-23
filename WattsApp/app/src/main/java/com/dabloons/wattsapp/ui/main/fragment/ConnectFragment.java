package com.dabloons.wattsapp.ui.main.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.manager.LightManager;
import com.dabloons.wattsapp.manager.auth.PhillipsHueOAuthManager;

public class ConnectFragment extends Fragment {

    private final String LOG_TAG = "ConnectFragment";

    private PhillipsHueOAuthManager phillipsHueOAuthManager = PhillipsHueOAuthManager.getInstance();

    public ConnectFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_connect, container, false);
        result.findViewById(R.id.button_connect_phillips_hue).setOnClickListener(view -> {
            phillipsHueOAuthManager.aquireAuthorizationCode(this.getActivity());
        });
        result.findViewById(R.id.button_sync_phillips_hue).setOnClickListener(view -> {
            LightManager.getInstance().syncLights();
        });
        return result;
    }
}
