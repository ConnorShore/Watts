package com.dabloons.wattsapp.ui.main.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.manager.auth.PhillipsHueOAuthManager;

public class ConnectFragment extends Fragment {

    private PhillipsHueOAuthManager oAuthManager = PhillipsHueOAuthManager.getInstance();

    public ConnectFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_connect, container, false);
        result.findViewById(R.id.button_connect_phillips_hue).setOnClickListener(view -> {
            oAuthManager.aquireAuthorizationCode(this.getActivity());
        });
        return result;
    }
}