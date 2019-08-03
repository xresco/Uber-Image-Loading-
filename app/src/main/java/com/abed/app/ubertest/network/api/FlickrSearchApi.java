package com.abed.app.ubertest.network.api;

import android.support.annotation.NonNull;

public class FlickrSearchApi extends FlickrBaseApi {

    private static final String GET_SEARCH_IMAGES_URL = "&method=flickr.photos.search";

    @NonNull
    @Override
    public RequestMethod getRequestMethod() {
        return RequestMethod.GET;
    }

    @NonNull
    @Override
    String getPath() {
        return GET_SEARCH_IMAGES_URL;
    }
}

