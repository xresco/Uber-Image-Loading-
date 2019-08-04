package com.abed.app.ubertest;

import android.app.Application;

import com.abed.app.ubertest.network.ImageDownloader;
import com.abed.app.ubertest.network.chache.FileCache;

public class UberTestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        ImageDownloader.init(new ImageMemoryCache());
        ImageDownloader.init(new FileCache(this));
    }
}
