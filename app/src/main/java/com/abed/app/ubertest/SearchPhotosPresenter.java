package com.abed.app.ubertest;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abed.app.ubertest.model.request.FlickrSearchRequest;
import com.abed.app.ubertest.model.response.FlickrSearchResponse;
import com.abed.app.ubertest.mvp.MvpPresenter;
import com.abed.app.ubertest.network.ApiCaller;
import com.abed.app.ubertest.network.DispatcherThread;
import com.abed.app.ubertest.network.RetryStrategy;
import com.abed.app.ubertest.network.api.FlickrSearchApi;

public class SearchPhotosPresenter extends MvpPresenter<SearchPhotosView>
        implements DispatcherThread.DispatcherCallback<FlickrSearchResponse> {

    private static final String TAG = "SEARCH_PHOTOS_PRESENTER";

    @NonNull
    private DispatcherThread<FlickrSearchRequest, FlickrSearchResponse> dispatcherThread;

    @NonNull
    private FlickrSearchRequest flickrSearchRequest;

    SearchPhotosPresenter() {
        DispatcherThread.DispatcherTask<FlickrSearchRequest, FlickrSearchResponse> dispatcherTask =
                request -> (new ApiCaller()).execute(new FlickrSearchApi(), request);

        dispatcherThread = new DispatcherThread<>(TAG,
                dispatcherTask,
                this,
                RetryStrategy.FIBBONACCI_RETRY_STRATEGY);

        flickrSearchRequest = new FlickrSearchRequest();

    }

    void loadMorePhoto() {
        getAttachedView().showLoadingState();
        dispatcherThread.requestDispatch(flickrSearchRequest);
    }

    public void searchPhotos(@NonNull String query) {
        getAttachedView().clearPhotos();
        getAttachedView().showLoadingState();
        flickrSearchRequest.setSearchText(query);
        dispatcherThread.requestDispatch(flickrSearchRequest);
    }


    @Override
    public void detach() {
        super.detach();
        dispatcherThread.shutdown();
    }

    @Override
    public void onSuccess(@Nullable FlickrSearchResponse result) {
        if (result == null) {
            getAttachedView().showErrorState();
            return;
        }
        getAttachedView().showPhotos(result.getFlickrPhotoList());
    }

    @Override
    public void onFail(int completionStatus, @Nullable Throwable throwable) {
        getAttachedView().showErrorState();
    }
}
