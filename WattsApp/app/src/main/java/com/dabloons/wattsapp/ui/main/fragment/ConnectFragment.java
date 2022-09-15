package com.dabloons.wattsapp.ui.main.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.MenuRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.IntegrationSceneManager;
import com.dabloons.wattsapp.manager.LightManager;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.manager.auth.NanoleafAuthManager;
import com.dabloons.wattsapp.manager.auth.PhillipsHueOAuthManager;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.model.integration.NanoleafPanelIntegrationAuth;
import com.dabloons.wattsapp.ui.main.adapters.IntegrationAdapter;
import com.dabloons.wattsapp.ui.room.adapters.LightItemAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import util.UIMessageUtil;
import util.UIUtil;

public class ConnectFragment extends Fragment {

    private final String LOG_TAG = "ConnectFragment";

    private final String DEVICES_FOUND_TEXT = "Devices found: ";

    private PhillipsHueOAuthManager phillipsHueOAuthManager = PhillipsHueOAuthManager.getInstance();
    private NanoleafAuthManager nanoleafAuthManager = NanoleafAuthManager.getInstance();
    private UserManager userManager = UserManager.getInstance();

    private Button syncLightsBtn;
    private TextView devicesFoundTextView;

    private IntegrationAdapter integrationAdapter;
    private RecyclerView integrationRV;
    private TextView emptyViewText;

    private LightItemAdapter<NanoleafPanelIntegrationAuth> discoveredLightsAdapter;
    private RecyclerView discoveredLightsRV;

    private AlertDialog popupDialog;
//    private MaterialAlertDialogBuilder popupDialogBuilder;

    private View discoveryView;
    private View selectLightsView;
    private View loadingView;

    private int devicesDiscoveredCt;

    public ConnectFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_connect, container, false);

        syncLightsBtn = result.findViewById(R.id.sync_lights_btn);
//        popupDialogBuilder = new MaterialAlertDialogBuilder(this.getContext());

        initializePopupItems();
        initializeListeners(result);
        emptyViewText = result.findViewById(R.id.emptyIntegrationListText);


        return result;
    }

    private void initializeListeners(View result) {
        result.findViewById(R.id.connect_to_integration_Btn).setOnClickListener(v -> showMenu(v, R.menu.popup_connect_menu));

        userManager.getUserIntegrations((integration, status) -> {
            if(integration.size() == 0)
                syncLightsBtn.setClickable(false);

            integrationAdapter = new IntegrationAdapter(WattsApplication.getAppContext(), (ArrayList<IntegrationType>) integration);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(WattsApplication.getAppContext(), LinearLayoutManager.VERTICAL, false);
            integrationRV = result.findViewById(R.id.integration_RV);
            integrationRV.setLayoutManager(linearLayoutManager);
            integrationRV.setAdapter(integrationAdapter);

            if(integrationAdapter.integrationTypeList == null || integrationAdapter.integrationTypeList.size() == 0)
            {
                UIUtil.toggleViews(integrationAdapter.integrationTypeList.size(), emptyViewText, integrationRV);
            }


            return null;
        });

        syncLightsBtn.setOnClickListener(view -> {
            LightManager.getInstance().syncLights();
            IntegrationSceneManager.getInstance().syncIntegrationScenes();
        });
    }

    private void showMenu(View v, @MenuRes int res)
    {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.getMenuInflater().inflate(res, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            switch(item.getTitle().toString()) {
                case "Phillips Hue":
                    phillipsHueOAuthManager.aquireAuthorizationCode();
                    break;

                case "Nanoleaf":
                    devicesDiscoveredCt = 0;
                    new AlertDialog.Builder(getActivity())
                            .setMessage("Are you panels in connect mode? (Hold power button down for 5-7 seconds)")
                            .setPositiveButton("Yes", (dialogInterface, i) -> {
                                nanoleafAuthManager.discoverNanoleafPanelsOnNetwork(
                                        (var, status) -> {
                                            // On device found callback
                                            if(status.success)
                                                updateDevicesFoundCount(++devicesDiscoveredCt);
                                            else
                                                Log.e(LOG_TAG, "Issue discovering device: " + status.message);
                                            return null;
                                        },
                                        (panels, status) -> {
                                            // On finished discovering devices callback
                                            devicesDiscoveredCt = 0;
                                            updateDevicesFoundCount(0);
                                            setLightsForSelection(panels);
                                            setSelectLightsView();
                                            return null;
                                        });
                                launchPopupWindow();
                            })
                            .setNegativeButton("No", null)
                            .show();
                    break;
                default:
                    Log.w(LOG_TAG, "Integration not found.");
                    break;
            }

            return false;
        });

        popupMenu.show();
    }

    private void launchPopupWindow() {
        closePopupWindow();
        MaterialAlertDialogBuilder popupDialogBuilder = new MaterialAlertDialogBuilder(this.getContext());
        if(discoveryView.getParent() != null)
            ((ViewGroup)discoveryView.getParent()).removeView(discoveryView);
        popupDialogBuilder.setView(discoveryView)
                .setTitle("Discover Lights")
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // stop discovering lights
                    nanoleafAuthManager.cancelDiscovery();
                    dialog.dismiss();
                });

        popupDialog = popupDialogBuilder.create();
        popupDialog.show();
    }

    private void initializePopupItems() {
        loadingView = LayoutInflater.from(this.getContext()).inflate(R.layout.loading_layout, null);
        discoveryView = LayoutInflater.from(this.getContext()).inflate(R.layout.discover_devices, null);
        selectLightsView = LayoutInflater.from(this.getContext()).inflate(R.layout.select_lights, null);

        devicesFoundTextView = discoveryView.findViewById(R.id.devices_found_txt);
        discoveredLightsAdapter = new LightItemAdapter<>(this.getContext(), new ArrayList<>());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(WattsApplication.getAppContext(), LinearLayoutManager.VERTICAL, false);
        discoveredLightsRV = selectLightsView.findViewById(R.id.discoveredLightsRV);
        discoveredLightsRV.setLayoutManager(linearLayoutManager);
        discoveredLightsRV.setAdapter(discoveredLightsAdapter);
    }

    private void updateDevicesFoundCount(int newCt) {
        String text = DEVICES_FOUND_TEXT + newCt;
        devicesFoundTextView.setText(text);
    }

    private void setLoadingView() {
        closePopupWindow();
        new Handler(Looper.getMainLooper()).post(() -> {
            TextView loadingTxt = loadingView.findViewById(R.id.loading_text);
            loadingTxt.setText("Adding devices...");
            MaterialAlertDialogBuilder popupDialogBuilder = new MaterialAlertDialogBuilder(this.getContext());
            if(loadingView.getParent() != null)
                ((ViewGroup)loadingView.getParent()).removeView(loadingView);
            popupDialogBuilder.setView(loadingView)
                    .setPositiveButton("", null)
                    .setNegativeButton("", null);

            popupDialog = popupDialogBuilder.create();
            popupDialog.show();
        });
    }

    private void setSelectLightsView() {
        closePopupWindow();
        new Handler(Looper.getMainLooper()).post(() -> {
            MaterialAlertDialogBuilder popupDialogBuilder = new MaterialAlertDialogBuilder(this.getContext());
            if(selectLightsView.getParent() != null)
                ((ViewGroup)selectLightsView.getParent()).removeView(selectLightsView);
            popupDialogBuilder.setView(selectLightsView)
                .setTitle("Select Lights To Add")
                .setPositiveButton("Confirm", (dialogInterface, i) ->  {
                    confirmLights();
                });

            popupDialog = popupDialogBuilder.create();
            popupDialog.show();
        });
    }

    private void confirmLights() {
        setLoadingView();
        List<NanoleafPanelIntegrationAuth> lights = discoveredLightsAdapter.getSelectedLights();
        nanoleafAuthManager.connectToPanels(lights, (numLights, status) -> {
            if(status.success) {
                String message;
                if(numLights > 0)
                    message = String.format("Successfully added %s Nanoleaf panels", numLights);
                else
                    message = "No lights were added";

                UIMessageUtil.showLongToastMessage(WattsApplication.getAppContext(), message);
            } else {
                UIMessageUtil.showLongToastMessage(WattsApplication.getAppContext(), "Failed to add panels");
                Log.e(LOG_TAG, status.message);
            }

            closePopupWindow();
            return null;
        });
    }

    private void setLightsForSelection(List<NanoleafPanelIntegrationAuth> panels) {
        discoveredLightsAdapter.setLights(panels);
    }

    private void closePopupWindow() {
        if(popupDialog != null)
            popupDialog.dismiss();
    }
}
