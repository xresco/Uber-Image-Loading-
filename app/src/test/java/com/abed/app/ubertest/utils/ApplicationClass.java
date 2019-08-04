package com.abed.app.ubertest.utils;

import android.app.Application;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.abed.app.ubertest.network.ImageDownloader;
import com.abed.app.ubertest.network.chache.ImageCache;

public class ApplicationClass extends Application {

    private final String TAG = getClass().getName();

    public ApplicationClass() {
        final ImageCache NOOP = new ImageCache() {
            @Nullable
            @Override
            public Bitmap get(@NonNull String id) {
                Log.d(TAG, "get: " + id);
                return null;
            }

            @Override
            public void put(@NonNull String id, @NonNull Bitmap bitmap) {
                Log.d(TAG, "put: " + id);
            }

            @Override
            public void clear() {
                Log.d(TAG, "clear: ");

            }
        };
        ImageDownloader.init(NOOP);
    }

}
