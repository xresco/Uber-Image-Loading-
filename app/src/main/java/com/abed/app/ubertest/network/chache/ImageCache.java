package com.abed.app.ubertest.network.chache;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface ImageCache {

    @Nullable
    Bitmap get(@NonNull String id);

    void put(@NonNull String id, @NonNull Bitmap bitmap);

    void clear();
}
