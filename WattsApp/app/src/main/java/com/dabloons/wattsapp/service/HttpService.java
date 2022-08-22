package com.dabloons.wattsapp.service;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.dabloons.wattsapp.repository.UserRepository;
import com.google.api.Http;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class HttpService {

    public enum RequestType {
        POST,
        GET,
        PUT,
        DELETE
    };

    private OkHttpClient httpClient;

    protected String baseUrl;

    public HttpService() {
        try {
            HandlerThread thread = new HandlerThread("okhttpclient_init");
            thread.start();
            Handler handler = new Handler(thread.getLooper());
            handler.post(() -> {
                httpClient = new OkHttpClient();
            });
        } catch(Exception e) {
            System.out.println("WTF");
        }
        setBaseUrl();
    }

    public abstract void setBaseUrl();

    protected void makeRequestWithBodyAsync(String path, RequestType requestType, RequestBody body, Map<String, String> headers, Callback callback) {
        Request request = buildRequest(path, requestType, body, headers).build();
        httpClient.newCall(request).enqueue(callback);
    }

    protected void makeRequestAsync(String path, RequestType requestType, Map<String, String> headers, Callback callback) {
        makeRequestWithBodyAsync(path, requestType, null, headers, callback);
    }

    protected RequestBody createRequestBody(JsonObject bodyObj) {
        String json = bodyObj.toString();
        return RequestBody.create(MediaType.parse("application/json; charset=utf-16"), json);
    }

    private Request.Builder buildRequest(String path, RequestType requestType, RequestBody body, Map<String, String> headers) {
        String url = this.baseUrl + path;

        Request.Builder requestBuilder = new Request.Builder()
                .url(url);

        switch(requestType) {
            case POST:
                if(body == null) Log.w("HttpService", "Body is null for post request");
                requestBuilder.post(body);
                break;
            case PUT:
                if(body == null) Log.w("HttpService", "Body is null for put request");
                requestBuilder.put(body);
                break;
            case GET:
                requestBuilder.get();
                break;
            case DELETE:
                requestBuilder.delete();
                break;
        }

        if(headers != null) {
            for(String key : headers.keySet()) {
                requestBuilder.header(key, headers.get(key));
            }
        }

        return requestBuilder;
    }

    public OkHttpClient getHttpClient() {
        return this.httpClient;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }
}
