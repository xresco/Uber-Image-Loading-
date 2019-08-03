package com.abed.app.ubertest.network.api;

import android.support.annotation.NonNull;

public abstract class FlickrBaseApi {


    private static final String MAIN_BASE_URL = "https://api.flickr.com/?&api_key=%s&";
    private static final String API_KEY = "fdb8bd80fcb4c3a79ef6a65f3c1a2253";

    @NonNull
    public final String getUrl() {
        return String.format(MAIN_BASE_URL, API_KEY) + getPath();
    }

    @NonNull
    public abstract RequestMethod getRequestMethod();

    @NonNull
    abstract String getPath();


    //Currently we only support GET
    public enum RequestMethod {
        GET;
    }
}
