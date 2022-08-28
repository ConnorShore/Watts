package util;

import com.dabloons.wattsapp.model.integration.IntegrationType;

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
}
