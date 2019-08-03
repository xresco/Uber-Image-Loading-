package com.abed.app.ubertest;

import android.support.annotation.NonNull;

import com.abed.app.ubertest.model.response.FlickrPhoto;
import com.abed.app.ubertest.mvp.MvpView;

import java.util.List;

public interface SearchPhotosView extends MvpView {

    void showPhotos(@NonNull List<FlickrPhoto> photos);

    void showLoadingState();

    void showErrorState();

    void clearPhotos();

}
