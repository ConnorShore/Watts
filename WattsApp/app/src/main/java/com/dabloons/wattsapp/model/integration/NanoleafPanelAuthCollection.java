package com.dabloons.wattsapp.model.integration;

import android.util.Log;

import com.dabloons.wattsapp.model.Light;
import com.google.firebase.firestore.Exclude;

import java.util.List;
import java.util.UUID;

public class NanoleafPanelAuthCollection extends IntegrationAuth {

    @Exclude
    private final String LOG_TAG = "NanoleafPanelAuthCollection";

    private List<NanoleafPanelIntegrationAuth> panelAuths;

    public NanoleafPanelAuthCollection(List<NanoleafPanelIntegrationAuth> panelAuths) {
        super(UUID.randomUUID().toString(), IntegrationType.NANOLEAF);
        this.panelAuths = panelAuths;
    }

    public void addNanoleafPanelAuth(NanoleafPanelIntegrationAuth auth) {
        this.panelAuths.add(auth);
    }

    public void removeNanoleafPanelAuth(NanoleafPanelIntegrationAuth auth) {
        this.panelAuths.remove(auth);
    }

    public NanoleafPanelIntegrationAuth findNanoleafPanelAuthForLight(Light light) {
        if(light.getIntegrationType() != IntegrationType.NANOLEAF) {
            Log.w(LOG_TAG, "Mismatch integration type finding light");
            return null;
        }

        for(NanoleafPanelIntegrationAuth auth : panelAuths) {
            if(auth.getUid().equals(light.getIntegrationId())) {
                return auth;
            }
        }

        return null;
    }

    public List<NanoleafPanelIntegrationAuth> getPanelAuths() {
        return panelAuths;
    }

    public void setPanelAuths(List<NanoleafPanelIntegrationAuth> panelAuths) {
        this.panelAuths = panelAuths;
    }
}
