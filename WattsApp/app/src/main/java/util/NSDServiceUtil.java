package util;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.model.NetworkService;

import java.util.HashMap;
import java.util.Map;

public class NSDServiceUtil {

    private final String LOG_TAG = "NSDServiceUtil";

    private static volatile NSDServiceUtil instance;

    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener nsdListener;

    private Map<String, WattsCallback<NetworkService, Void>> onDiscoveryCallbacks;

    public NSDServiceUtil() {
        nsdManager = (NsdManager) WattsApplication.getAppContext().getSystemService(Context.NSD_SERVICE);
        onDiscoveryCallbacks = new HashMap<>();
        initializeDiscoveryListener();
    }

    public void clearDiscoveryCallbacks() {
        onDiscoveryCallbacks.clear();
    }

    public void initializeDiscoveryListener() {
        nsdListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.d(LOG_TAG, "Found service: " + serviceInfo.getServiceType());
                if(onDiscoveryCallbacks.containsKey(serviceInfo.getServiceType()))
                    nsdManager.resolveService(serviceInfo, new NSDResolveListener());
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(LOG_TAG, "Failed to start discover " + serviceType + "; error=" + errorCode);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(LOG_TAG, "Failed to stop discovery " + serviceType + "; error=" + errorCode);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.d(LOG_TAG, "Discovery began " + serviceType);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.d(LOG_TAG, "Discovery stopped " + serviceType);
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.w(LOG_TAG, "Lost service " + serviceInfo.getServiceType());
            }
        };
    }

    public void discoverService(String serviceType, WattsCallback<NetworkService, Void> callback) {
        onDiscoveryCallbacks.put(serviceType, callback);
        nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, nsdListener);
    }

    public void safeEndNetworkDiscovery(WattsCallback<Boolean, Void> callback) {
        if(nsdListener == null) {
            callback.apply(true, new WattsCallbackStatus(true));
            return;
        }

        if(onDiscoveryCallbacks.size() > 0) {
            Log.w(LOG_TAG, "There are still callbacks awaiting resolve");
            callback.apply(false, new WattsCallbackStatus(true));
            return;
        }

        this.nsdManager.stopServiceDiscovery(nsdListener);
        this.nsdListener = null;
        callback.apply(true, new WattsCallbackStatus(true));
    }

    public void forceEndNetworkDiscovery() {
        if(nsdListener == null)
            return;

        this.nsdManager.stopServiceDiscovery(nsdListener);
        this.nsdListener = null;
        clearDiscoveryCallbacks();
    }

    public static NSDServiceUtil getInstance() {
        NSDServiceUtil result = instance;
        if (result != null) {
            return result;
        }
        synchronized(NSDServiceUtil.class) {
            if (instance == null) {
                instance = new NSDServiceUtil();
            }
            return instance;
        }
    }

    class NSDResolveListener implements NsdManager.ResolveListener {
        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.e(LOG_TAG, "Failed to resolve service: " + serviceInfo.getServiceType() + "; error=" + errorCode);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Log.d(LOG_TAG, "Resolve Succeeded. " + serviceInfo);
            String type = serviceInfo.getServiceType();
            if(!onDiscoveryCallbacks.containsKey(type)) {
                // period sometimes disappears in beginning (need add to end to match initial key)
                type = type.substring(1) + ".";
            }

            if(onDiscoveryCallbacks.containsKey(type)) {
                NetworkService retService = new NetworkService(serviceInfo.getServiceType(),
                        serviceInfo.getServiceName(), serviceInfo.getPort(), serviceInfo.getHost());

                WattsCallback<NetworkService, Void> callback = onDiscoveryCallbacks.get(type);
                callback.apply(retService, new WattsCallbackStatus(true));
                onDiscoveryCallbacks.remove(type);
            }
        }
    };
}
