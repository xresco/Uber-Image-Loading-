package com.abed.app.ubertest;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.abed.app.ubertest.model.response.FlickrPhoto;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchPhotosView {

    @Nullable
    private SearchPhotosPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        presenter = new SearchPhotosPresenter();
        presenter.attach(this);
        presenter.loadMorePhoto();
    }

    @Override
    public void showPhotos(@NonNull List<FlickrPhoto> photos) {

    }

    @Override
    public void showErrorState() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detach();
        }
    }

    @Override
    public void clearPhotos() {

    }

    @Override
    public void showLoadingState() {

    }
}
