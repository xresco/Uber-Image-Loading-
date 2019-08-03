package com.abed.app.ubertest.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class Logger {

    public static void logError(@NonNull String tag, @NonNull String msg, @Nullable Throwable throwable) {
        Log.e(tag, msg, throwable);
    }

    public static void logWarning(@NonNull String tag, @NonNull String msg) {
        Log.w(tag, msg);

    }

    public static void logEvent(@NonNull String tag, @NonNull String msg) {
        Log.i(tag, msg);
    }
}
