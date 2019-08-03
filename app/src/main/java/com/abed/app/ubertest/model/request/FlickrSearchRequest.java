package com.abed.app.ubertest.model.request;


import android.support.annotation.NonNull;

import java.util.Locale;

public class FlickrSearchRequest implements ApiRequestData {

    private int pageNo;

    @NonNull
    private String searchText;

    public FlickrSearchRequest() {
        this("");
    }

    public FlickrSearchRequest(@NonNull String searchText) {
        this.pageNo = 0;
        this.searchText = searchText;
    }

    public void nextPage() {
        this.pageNo++;
    }

    public void setSearchText(@NonNull String searchText) {
        this.searchText = searchText;
    }

    @NonNull
    @Override
    public String toUrlParameters() {
        return String.format(Locale.US, "&text=\"%s\"&page=%d", searchText, pageNo);
    }
}
