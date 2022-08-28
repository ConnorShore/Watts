package util;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.model.NetworkService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class NSDServiceUtil {

    private final String LOG_TAG = "NSDServiceUtil";

    private static volatile NSDServiceUtil instance;

    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener nsdListener;
    private NsdManager.ResolveListener nsdResolveListener;

    private AtomicBoolean resolveListenerBusy;

    private ConcurrentMap<String, WattsCallback<NetworkService, Void>> onDiscoveryCallbacks;
    private ConcurrentLinkedQueue<NsdServiceInfo> pendingNsdServices;
    private ConcurrentLinkedQueue<NsdServiceInfo> resolvedNsdServices;

    private NSDServiceUtil() {
        nsdManager = (NsdManager) WattsApplication.getAppContext().getSystemService(Context.NSD_SERVICE);

        resolveListenerBusy = new AtomicBoolean(false);
        onDiscoveryCallbacks = new ConcurrentHashMap<>();
        pendingNsdServices = new ConcurrentLinkedQueue<>();
        resolvedNsdServices = new ConcurrentLinkedQueue<>();

        nsdResolveListener = new NSDResolveListener();

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
                if(onDiscoveryCallbacks.containsKey(serviceInfo.getServiceType())) {

                    if(resolveListenerBusy.compareAndSet(false, true))
                        nsdManager.resolveService(serviceInfo, nsdResolveListener);
                    else
                        pendingNsdServices.add(serviceInfo);
                }
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(LOG_TAG, "Failed to start discover " + serviceType + "; error=" + errorCode);
                forceEndNetworkDiscovery();
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(LOG_TAG, "Failed to stop discovery " + serviceType + "; error=" + errorCode);
                nsdManager.stopServiceDiscovery(this);
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
                Iterator<NsdServiceInfo> iterator = pendingNsdServices.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().getServiceName() == serviceInfo.getServiceName())
                        iterator.remove();
                }

                // If the lost service was in the list of resolved services, remove it
                synchronized(resolvedNsdServices) {
                    iterator = resolvedNsdServices.iterator();
                    while (iterator.hasNext()) {
                        if (iterator.next().getServiceName() == serviceInfo.getServiceName())
                            iterator.remove();
                    }
                }
            }
        };
    }

    public void discoverService(String serviceType, WattsCallback<NetworkService, Void> callback) {
        try {
            onDiscoveryCallbacks.put(serviceType, callback);
            nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, nsdListener);
        } catch(Exception e) {
            Log.w(LOG_TAG, e.getMessage());
            restartDiscoveryService();
            discoverService(serviceType, callback);
        }
    }

    private void restartDiscoveryService() {
        forceEndNetworkDiscovery();
        initializeDiscoveryListener();
    }

    public void removeDiscoveryCallback(String serviceType) {
        onDiscoveryCallbacks.remove(serviceType);
    }

    public void waitForAllServicesToResolve(WattsCallback<Void, Void> callback) {
        if(pendingNsdServices.size() == 0) {
            callback.apply(null, new WattsCallbackStatus(true));
            return;
        }

        resolveNextInQueue();

        try {
            Thread.sleep(500);
            waitForAllServicesToResolve(callback);
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, e.getMessage());
            callback.apply(null, new WattsCallbackStatus(false, e.getMessage()));
            return;
        }
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
        clearDiscoveryCallbacks();
        if(nsdListener == null)
            return;

        this.nsdManager.stopServiceDiscovery(nsdListener);
        this.nsdListener = null;
    }

    private void resolveNextInQueue() {
        NsdServiceInfo next = pendingNsdServices.poll();
        if(next != null)
            nsdManager.resolveService(next, nsdResolveListener);
        else
            resolveListenerBusy.set(false);
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
            resolveNextInQueue();
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Log.d(LOG_TAG, "Resolve Succeeded. " + serviceInfo);

            resolvedNsdServices.add(serviceInfo);

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
            }

            resolveNextInQueue();
        }
    };
}
