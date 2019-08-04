package com.abed.app.ubertest.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.abed.app.ubertest.model.request.ApiRequestData;
import com.abed.app.ubertest.model.response.ApiResponse;
import com.abed.app.ubertest.network.api.FlickrBaseApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiCaller {

    private static final String CHARSET_NAME = "UTF-8";
    private static final int TIMEOUT = 3000;

    @NonNull
    @WorkerThread
    public static <R extends ApiResponse> R execute(@NonNull FlickrBaseApi<R> api, @Nullable ApiRequestData data) throws IOException, ValidationException {
        String urlString = data == null ? api.getUrl() : api.getUrl() + data.toUrlParameters();
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        connection.setRequestProperty("Content-Type", "application/json contentType");
        connection.setRequestMethod(api.getRequestMethod().name());
        connection.setRequestProperty("Charset", CHARSET_NAME);
        StringBuilder resultStringBuilder = new StringBuilder();

        BufferedReader bufReader;

        if (connection.getResponseCode() == 200) {
            bufReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } else {
            bufReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }


        try {
            String line = bufReader.readLine();

            while (line != null) {
                resultStringBuilder.append(line);
                line = bufReader.readLine();
            }

        } finally {
            connection.disconnect();
            bufReader.close();
        }

        String response = resultStringBuilder.toString();
        return api.getParser().parse(response);
    }

}
