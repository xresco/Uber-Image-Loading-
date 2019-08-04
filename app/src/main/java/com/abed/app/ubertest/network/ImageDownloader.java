package com.abed.app.ubertest.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.abed.app.ubertest.network.chache.ImageCache;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageDownloader {

    @NonNull
    private ImageCache cache;

    private static ImageDownloader instance;

    public static ImageDownloader getInstance() {
        if (instance == null) {
            instance = new ImageDownloader();
        }
        return instance;
    }

    public static void init(ImageCache cache) {
        instance = new ImageDownloader(cache);

    }

    private ImageDownloader() {
        //To save a lot of ugly nullability checks
        cache = new ImageCache() {
            @Nullable
            @Override
            public Bitmap get(@NonNull String id) {
                return null;
            }

            @Override
            public void put(@NonNull String id, @NonNull Bitmap bitmap) {

            }

            @Override
            public void clear() {

            }
        };
    }

    private ImageDownloader(@NonNull ImageCache cache) {
        this.cache = cache;
    }

    @WorkerThread
    @Nullable
    public Bitmap getBitmapFromUrl(String urlString, int reqWidth, int reqHeight) {
        Bitmap bitmap = cache.get(urlString);

        if (bitmap != null) {
            return bitmap;
        }

        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.connect();
            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                bitmap = decodeSampledBitmapFromInputStream(connection.getInputStream(), reqWidth, reqHeight);
                cache.put(urlString, bitmap);
                return bitmap;
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }


    private Bitmap decodeSampledBitmapFromInputStream(InputStream inputStream, int reqWidth, int reqHeight) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        InputStream is = new BufferedInputStream(inputStream);
        is.mark(is.available());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        is.reset();
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(is, null, options);
    }


    private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}


