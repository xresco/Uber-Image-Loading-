package com.abed.app.ubertest.network.api;

import android.support.annotation.NonNull;

import com.abed.app.ubertest.model.ApiResponse;
import com.abed.app.ubertest.network.parser.FlickrResponseParser;

public abstract class FlickrBaseApi<R extends ApiResponse> {


    private static final String MAIN_BASE_URL = "https://api.flickr.com/?&api_key=%s&";
    private static final String API_KEY = "fdb8bd80fcb4c3a79ef6a65f3c1a2253";

    @NonNull
    public final String getUrl() {
        return String.format(MAIN_BASE_URL, API_KEY) + getPath();
    }

    @NonNull
    abstract String getPath();

    @NonNull
    public abstract RequestMethod getRequestMethod();

    @NonNull
    public abstract FlickrResponseParser<R> getParser();

    //Currently we only support GET
    public enum RequestMethod {
        GET
    }
}
