package com.abed.app.ubertest.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface Logger {

    void logError(@NonNull String tag, @NonNull String msg, @Nullable Throwable throwable);

    void logWarning(@NonNull String tag, @NonNull String msg);

    void logEvent(@NonNull String tag, @NonNull String msg);
}
