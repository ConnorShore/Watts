package com.dabloons.wattsapp.service;

import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.model.integration.PhillipsHueIntegrationAuth;
import com.google.android.gms.tasks.Task;

import java.io.IOException;

import io.reactivex.Observable;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.*;

public interface PhillipsHueService {


    // TODO: USer OkHttp instead of rrequests
    OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
        @Override
        public Response intercept(Chain chain) {
            Task<Response> res = UserManager.getInstance().getIntegrationAuthData(IntegrationType.PHILLIPS_HUE).continueWith(task -> {
                PhillipsHueIntegrationAuth auth = (PhillipsHueIntegrationAuth) task.getResult();

                Request request = chain.request().newBuilder()
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "Bearer " + auth.getAccessToken())
                        .build();
                return chain.proceed(request);
            });
        }
    });

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.meethue.com/route/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpClient.build())
            .build();

    @PUT("0/config")
    Call<Void> linkButton(@Body String body);

    @POST
    Call<String> getUsername(@Body String body);
}
