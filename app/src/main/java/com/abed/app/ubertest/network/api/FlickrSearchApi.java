package com.abed.app.ubertest.network.api;

import android.support.annotation.NonNull;

import com.abed.app.ubertest.model.FlickrSearchResponse;
import com.abed.app.ubertest.network.parser.FlickrResponseParser;
import com.abed.app.ubertest.network.parser.FlickrSearchResponseParser;

public class FlickrSearchApi extends FlickrBaseApi<FlickrSearchResponse> {

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

    @NonNull
    @Override
    public FlickrResponseParser<FlickrSearchResponse> getParser() {
        return new FlickrSearchResponseParser();
    }
}

