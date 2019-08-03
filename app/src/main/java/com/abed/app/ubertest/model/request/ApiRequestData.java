package com.abed.app.ubertest.model.request;

import android.support.annotation.NonNull;

public interface ApiRequestData {

    @NonNull
    String toUrlParameters();
}
