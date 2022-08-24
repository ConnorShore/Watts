package com.dabloons.wattsapp.manager.auth;

import android.util.Log;

import util.NSDServiceUtil;

public class NanoleafAuthManager {

    public final String LOG_TAG = "NanoleafAuthManager";

    public static volatile NanoleafAuthManager instance;

    private NSDServiceUtil nsdServiceUtil = NSDServiceUtil.getInstance();

    private int DEFAULT_PORT = 16021;
    private String NANOLEAF_MDNS_SERVICE = "_nanoleafapi._tcp.";

    public NanoleafAuthManager() {

    }

    public void connectToNanoleafsOnNetwork() {
        this.nsdServiceUtil.discoverService(NANOLEAF_MDNS_SERVICE, (service, status) -> {
            System.out.println(service);
            if(!service.getName().contains("Nanoleaf")) //Might need canvas instead of nanoleaf (have to test api to find out)
                return null;

            nsdServiceUtil.safeEndNetworkDiscovery((ended, status1) -> {
                if(!ended)
                    Log.e(LOG_TAG, "Failed to end network discovery after nanoleaf connection");
                else
                    Log.d(LOG_TAG, "Successfully ended network discovery after nanoleaf connection");
                return null;
            });
            return null;
        });
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
