package com.abed.app.ubertest.network.chache;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ImageMemoryCache implements ImageCache {

    private Map<String, SoftReference<Bitmap>> cache = Collections.synchronizedMap(new HashMap<>());

    @Nullable
    @Override
    public Bitmap get(@NonNull String id) {
        if (!cache.containsKey(id)) {
            return null;
        }
        SoftReference<Bitmap> ref = cache.get(id);

        if (ref == null) {
            return null;
        }

        return ref.get();
    }

    @Override
    public void put(@NonNull String id, @NonNull Bitmap bitmap) {
        cache.put(id, new SoftReference<>(bitmap));
    }

    @Override
    public void clear() {
        cache.clear();
    }

}

