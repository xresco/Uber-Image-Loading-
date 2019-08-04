package com.abed.app.ubertest.screens.search;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.abed.app.ubertest.model.request.FlickrSearchRequest;
import com.abed.app.ubertest.model.response.FlickrSearchResponse;
import com.abed.app.ubertest.mvp.MvpPresenter;
import com.abed.app.ubertest.network.ApiCaller;
import com.abed.app.ubertest.network.RetryStrategy;
import com.abed.app.ubertest.network.api.FlickrSearchApi;
import com.abed.app.ubertest.network.dispatcher.Scheduler;
import com.abed.app.ubertest.network.dispatcher.SingleThreadScheduler;

public class SearchPhotosPresenter extends MvpPresenter<SearchPhotosView>
        implements Scheduler.SchedulerCallback<FlickrSearchResponse> {

    private static final String TAG = "SEARCH_PHOTOS_PRESENTER";

    @NonNull
    private SingleThreadScheduler<FlickrSearchRequest, FlickrSearchResponse> singleThreadScheduler;

    @NonNull
    private FlickrSearchRequest flickrSearchRequest;

    @VisibleForTesting
    public SearchPhotosPresenter() {
        Scheduler.SchedulerTask<FlickrSearchRequest, FlickrSearchResponse>
                dispatcherTask = request -> ApiCaller.execute(new FlickrSearchApi(), request);

        singleThreadScheduler = new SingleThreadScheduler<>(TAG,
                dispatcherTask,
                RetryStrategy.FIBBONACCI_RETRY_STRATEGY);

        flickrSearchRequest = new FlickrSearchRequest();

    }

    void loadMorePhoto() {
        getAttachedView().showLoadingState();
        flickrSearchRequest.nextPage();
        singleThreadScheduler.requestDispatch(flickrSearchRequest, this);
    }

    public void searchPhotos(@NonNull String query) {
        getAttachedView().clearPhotos();
        getAttachedView().showLoadingState();
        flickrSearchRequest.setSearchText(query);
        singleThreadScheduler.requestDispatch(flickrSearchRequest, this);
    }


    @Override
    public void detach() {
        super.detach();
        singleThreadScheduler.shutdown();
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
