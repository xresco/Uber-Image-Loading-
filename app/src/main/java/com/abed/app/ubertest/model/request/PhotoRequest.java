package com.abed.app.ubertest.model.request;


import android.support.annotation.NonNull;

public class PhotoRequest {

    private int reqWidth;

    private int reqHeight;

    @NonNull
    private String url;

    public PhotoRequest(@NonNull String url, int reqWidth, int reqHeight) {
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
        this.url = url;
    }

    public int getReqWidth() {
        return reqWidth;
    }

    public int getReqHeight() {
        return reqHeight;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

}
