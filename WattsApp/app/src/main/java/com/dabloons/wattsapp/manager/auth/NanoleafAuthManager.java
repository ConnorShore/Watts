package com.dabloons.wattsapp.manager.auth;

import android.util.Log;

import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.LightManager;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.model.integration.NanoleafPanelAuthCollection;
import com.dabloons.wattsapp.model.integration.NanoleafPanelIntegrationAuth;
import com.dabloons.wattsapp.service.NanoleafService;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import util.NSDServiceUtil;
import util.UIMessageUtil;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class NanoleafAuthManager {

    public final String LOG_TAG = "NanoleafAuthManager";

    public static volatile NanoleafAuthManager instance;

    private NSDServiceUtil nsdServiceUtil = NSDServiceUtil.getInstance();
    private NanoleafService nanoleafService = NanoleafService.getInstance();

    private UserManager userManager = UserManager.getInstance();
    private LightManager lightManager = LightManager.getInstance();

    private final String NANOLEAF_MDNS_SERVICE = "_nanoleafapi._tcp.";
    private final String URL_FORMAT = "http://%s:%s/api/v1/";

    public NanoleafAuthManager() {

    }

    public void discoverNanoleafPanelsOnNetwork() {
        List<NanoleafPanelIntegrationAuth> nanoleafPanelConnections = new ArrayList<>();

        // Set timeout of discovery
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                nsdServiceUtil.removeDiscoveryCallback(NANOLEAF_MDNS_SERVICE);
                nsdServiceUtil.safeEndNetworkDiscovery((ended, status1) -> {
                    if(!ended)
                        Log.e(LOG_TAG, "Failed to end network discovery after nanoleaf connection");
                    else
                        Log.d(LOG_TAG, "Successfully ended network discovery after nanoleaf connection");

                    getAuthTokenForProps(nanoleafPanelConnections, 0, (auths, status) -> {
                        if(!status.success) {
                            Log.e(LOG_TAG, status.message);
                            return null;
                        }

                        saveNanoleafPanelAuthsToDB(auths);
                        return null;
                    });
                    return null;
                });
            }
        }, 6000);

        // Start to discover panels
        this.nsdServiceUtil.discoverService(NANOLEAF_MDNS_SERVICE, (service, status) -> {
            // url: http://{hostName}:{port}/api/v1/
            String url = String.format(URL_FORMAT,
                    service.getHost().getHostName(), service.getPort());

            NanoleafPanelIntegrationAuth auth = new NanoleafPanelIntegrationAuth(service.getName(), url, null);
            nanoleafPanelConnections.add(auth);
            return null;
        });
    }

    private void getAuthTokenForProps(List<NanoleafPanelIntegrationAuth> authProps,
                                      int index, WattsCallback<List<NanoleafPanelIntegrationAuth>, Void> callback) {
        if(index >= authProps.size()) {
            callback.apply(authProps, new WattsCallbackStatus(true));
            return;
        }

        NanoleafPanelIntegrationAuth auth = authProps.get(0);
        nanoleafService.addNanoleafUser(auth, (authToken, status) -> {
            if(!status.success)
                Log.e(LOG_TAG, status.message);
            else
                auth.setAuthToken(authToken);

            int nextInd = index+1;
            getAuthTokenForProps(authProps, nextInd, callback);
            return null;
        });
    }

    private void saveNanoleafPanelAuthsToDB(List<NanoleafPanelIntegrationAuth> panelAuths) {
        List<NanoleafPanelIntegrationAuth> finalAuths = removeUnconnectedAuths(panelAuths);
        NanoleafPanelAuthCollection authCollection = new NanoleafPanelAuthCollection(finalAuths);
        userManager.addIntegrationAuthData(IntegrationType.NANOLEAF, authCollection, (var, status) -> {
            if(!status.success) {
                Log.e(LOG_TAG, status.message);
                return null;
            }

            lightManager.syncNanoleafLightsToDatabase(authCollection, (var1, status1) -> {
                if(!status1.success) {
                    Log.e(LOG_TAG, status1.message);
                    return null;
                }

                String message = String.format("Successfully added %s nanoleaf panels", panelAuths.size());
                UIMessageUtil.showLongToastMessage(WattsApplication.getAppContext(), message);
                return null;
            });

            return null;
        });
    }

    private List<NanoleafPanelIntegrationAuth> removeUnconnectedAuths(List<NanoleafPanelIntegrationAuth> panelAuths) {
        List<NanoleafPanelIntegrationAuth> finalAuths = new ArrayList<>();
        for(NanoleafPanelIntegrationAuth auth : panelAuths) {
            if(auth.getAuthToken() == null || auth.getAuthToken().length() == 0)
                continue;

            finalAuths.add(auth);
        }
        return finalAuths;
    }

    public static NanoleafAuthManager getInstance() {
        NanoleafAuthManager result = instance;
        if (result != null) {
            return result;
        }
        synchronized(NanoleafAuthManager.class) {
            if (instance == null) {
                instance = new NanoleafAuthManager();
            }
            return instance;
        }
    }
}
