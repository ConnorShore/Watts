package util;

import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.Light;
import com.dabloons.wattsapp.model.LightState;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.model.integration.NanoleafPanelAuthCollection;
import com.dabloons.wattsapp.model.integration.NanoleafPanelIntegrationAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RepositoryUtil {

    public static IntegrationType stringToIntegrationType(String s) {
        if(s == null || s.length() == 0)
            return null;

        switch(s) {
            case "PHILLIPS_HUE":
                return IntegrationType.PHILLIPS_HUE;
            case "NANOLEAF":
                return IntegrationType.NANOLEAF;
            default:
                return IntegrationType.NONE;
        }
    }

    public static List<Light> createNanoleafLightsFromAuthCollection(NanoleafPanelAuthCollection collection, Map<String, LightState> states) {
        String userId = UserManager.getInstance().getCurrentUser().getUid();
        List<Light> ret = new ArrayList<>();
        for(NanoleafPanelIntegrationAuth auth : collection.getPanelAuths()) {
            LightState state = states.get(auth.getName());
            if(state == null)
                state = new LightState();

            Light light = new Light(userId, auth.getName(), auth.getUid(), IntegrationType.NANOLEAF, state);
            ret.add(light);
        }
        return ret;
    }
}
