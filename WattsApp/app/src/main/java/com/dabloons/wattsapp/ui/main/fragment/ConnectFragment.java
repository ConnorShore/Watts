package com.dabloons.wattsapp.ui.main.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;

import androidx.annotation.MenuRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.LightManager;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.manager.auth.NanoleafAuthManager;
import com.dabloons.wattsapp.manager.auth.PhillipsHueOAuthManager;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.ui.main.adapters.IntegrationAdapter;

import java.util.ArrayList;
import java.util.List;

import util.WattsCallback;
import util.WattsCallbackStatus;

public class ConnectFragment extends Fragment {

    private final String LOG_TAG = "ConnectFragment";

    private PhillipsHueOAuthManager phillipsHueOAuthManager = PhillipsHueOAuthManager.getInstance();
    private NanoleafAuthManager nanoleafAuthManager = NanoleafAuthManager.getInstance();


    private RecyclerView intergrationRV;
    private IntegrationAdapter integrationAdapter;
    private Button syncLightsBtn;

    public ConnectFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_connect, container, false);
        syncLightsBtn = result.findViewById(R.id.sync_lights_btn);
        result.findViewById(R.id.connect_to_integration_Btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(v, R.menu.popup_connect_menu);
            }
        });

        UserManager.getInstance().getUserIntegrations(new WattsCallback<List<IntegrationType>, Void>() {
            @Override
            public Void apply(List<IntegrationType> var, WattsCallbackStatus status) {
                if(var.size() == 0)
                {
                    syncLightsBtn.setClickable(false);
                }
                integrationAdapter = new IntegrationAdapter(WattsApplication.getAppContext(), (ArrayList<IntegrationType>) var);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(WattsApplication.getAppContext(), LinearLayoutManager.VERTICAL, false);
                intergrationRV = result.findViewById(R.id.integration_RV);
                intergrationRV.setLayoutManager(linearLayoutManager);
                intergrationRV.setAdapter(integrationAdapter);


                return null;
            }
        });

        syncLightsBtn.setOnClickListener(view -> {
            LightManager.getInstance().syncLights();
        });

        return result;
    }

    private void showMenu(View v, @MenuRes int res)
    {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.getMenuInflater().inflate(res, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getTitle().toString()) {
                    case "Phillips Hue":
                        phillipsHueOAuthManager.aquireAuthorizationCode();

                    case "Nanoleef":
                        new AlertDialog.Builder(getActivity())
                                .setMessage("Are you panels in connect mode? (Hold power button down for 5-7 seconds)")
                                .setPositiveButton("Yes", (dialogInterface, i) ->
                                        nanoleafAuthManager.discoverNanoleafPanelsOnNetwork())
                                .setNegativeButton("No", null)
                                .show();
                    default:
                        Log.w(LOG_TAG, "Integration not found.");
                }
                return false;
            }
        });

        popupMenu.show();
    }
}
