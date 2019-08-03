package com.abed.app.ubertest.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ValidationException extends Exception {

    public ValidationException(@NonNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

}
