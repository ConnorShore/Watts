package com.dabloons.wattsapp.manager.auth;

import android.util.Log;

import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.LightManager;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.NetworkService;
import com.dabloons.wattsapp.model.integration.IntegrationAuth;
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

    private final int DISCOVERY_SEARCH_TIME_MILLISECONDS = 4000;

    public NanoleafAuthManager() {

    }

    public void discoverNanoleafPanelsOnNetwork(WattsCallback<NetworkService, Void> onDeviceFound,
                                                WattsCallback<List<NanoleafPanelIntegrationAuth>, Void> onFinish) {
        List<NanoleafPanelIntegrationAuth> nanoleafPanelConnections = new ArrayList<>();

        // Set timeout of discovery
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                nsdServiceUtil.safeEndNetworkDiscovery((ended, status) -> {
                    if(!ended)
                        Log.e(LOG_TAG, "Failed to end network discovery after nanoleaf connection");
                    else
                        Log.d(LOG_TAG, "Successfully ended network discovery after nanoleaf connection");

                    nsdServiceUtil.waitForAllServicesToResolve((var, status1) -> {
                        // Call callback here and move getAuthTokenForProps
                        if(!status.success)
                            Log.e(LOG_TAG, status1.message);

                        onFinish.apply(nanoleafPanelConnections, status1);
                        return null;
                    });

                    return null;
                });
            }
        }, DISCOVERY_SEARCH_TIME_MILLISECONDS);

        // Start to discover panels
        this.nsdServiceUtil.discoverService(NANOLEAF_MDNS_SERVICE, (service, status) -> {
            onDeviceFound.apply(service, status);

            // url: http://{hostName}:{port}/api/v1/
            String url = String.format(URL_FORMAT,
                    service.getHost().getHostName(), service.getPort());

            NanoleafPanelIntegrationAuth auth = new NanoleafPanelIntegrationAuth(service.getName(), url, null);
            nanoleafPanelConnections.add(auth);
            return null;
        });
    }


    public void cancelDiscovery() {
        nsdServiceUtil.forceEndNetworkDiscovery();
    }

    public void connectToPanels(List<NanoleafPanelIntegrationAuth> panels, WattsCallback<Integer, Void> callback) {
        getAuthTokenForProps(panels, 0, (auths, status) -> {
            if(!status.success) {
                Log.e(LOG_TAG, status.message);
                callback.apply(null, status);
                return null;
            }

            saveNanoleafPanelAuthsToDB(auths, callback);
            return null;
        });
    }

    private void getAuthTokenForProps(List<NanoleafPanelIntegrationAuth> authProps,
                                      int index, WattsCallback<List<NanoleafPanelIntegrationAuth>, Void> callback) {
        if(index >= authProps.size()) {
            callback.apply(authProps, new WattsCallbackStatus(true));
            return;
        }

        NanoleafPanelIntegrationAuth auth = authProps.get(index);
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

    private void saveNanoleafPanelAuthsToDB(List<NanoleafPanelIntegrationAuth> panelAuths, WattsCallback<Integer, Void> callback) {
        List<NanoleafPanelIntegrationAuth> finalAuths = removeUnconnectedAuths(panelAuths);
        userManager.getIntegrationAuthData(IntegrationType.NANOLEAF, (collection, status) -> {
            if(!status.success) {
                Log.e(LOG_TAG, status.message);
                callback.apply(null, status);
                return null;
            }

            NanoleafPanelAuthCollection authCollection;
            if(collection == null)
                authCollection = new NanoleafPanelAuthCollection(finalAuths);
            else {
                authCollection = (NanoleafPanelAuthCollection) collection;
                for(NanoleafPanelIntegrationAuth auth : finalAuths) {
                    authCollection.addNanoleafPanelAuth(auth);
                }
            }

            userManager.addIntegrationAuthData(IntegrationType.NANOLEAF, authCollection, (var, status1) -> {
                if(!status1.success) {
                    Log.e(LOG_TAG, status1.message);
                    callback.apply(null, status);
                    return null;
                }

                lightManager.syncNanoleafLightsToDatabase(authCollection, (var1, status11) -> {
                    if(!status11.success) {
                        Log.e(LOG_TAG, status11.message);
                        return null;
                    }

                    callback.apply(finalAuths.size(), status11);
                    return null;
                });

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
