package com.dabloons.wattsapp.service.streams;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.service.PhillipsHueService;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;

public class PhillipsHueStreams {

    public static Call linkButton() {
        PhillipsHueService service = PhillipsHueService.retrofit.create(PhillipsHueService.class);

        JsonObject body = new JsonObject();
        body.addProperty("linkbutton", true);
        String bodyString = body.toString();

        return service.linkButton(bodyString);
    }

    public static Call<String> getUsername() throws IOException {
        PhillipsHueService service = PhillipsHueService.retrofit.create(PhillipsHueService.class);

        JsonObject body = new JsonObject();
        body.addProperty("devicetype", UserManager.getInstance().getCurrentUser().getUid());
        String bodyString = body.getAsString();

        return service.getUsername(bodyString);
    }
}
