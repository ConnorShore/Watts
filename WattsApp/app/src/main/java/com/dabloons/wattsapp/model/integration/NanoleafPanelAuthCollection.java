package com.dabloons.wattsapp.model.integration;

import java.util.List;
import java.util.UUID;

public class NanoleafPanelAuthCollection extends IntegrationAuth {

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

    public List<NanoleafPanelIntegrationAuth> getPanelAuths() {
        return panelAuths;
    }

    public void setPanelAuths(List<NanoleafPanelIntegrationAuth> panelAuths) {
        this.panelAuths = panelAuths;
    }
}
