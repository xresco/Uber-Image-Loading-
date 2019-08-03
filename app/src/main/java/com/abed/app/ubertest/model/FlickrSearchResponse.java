package com.abed.app.ubertest.model;

import java.util.List;

public class FlickrSearchResponse implements ApiResponse {

    private int pages;

    private List<FlickrPhoto> flickrPhotoList;

    public FlickrSearchResponse(int pages, List<FlickrPhoto> flickrPhotoList) {
        this.pages = pages;
        this.flickrPhotoList = flickrPhotoList;
    }

    public int getPages() {
        return pages;
    }

    public List<FlickrPhoto> getFlickrPhotoList() {
        return flickrPhotoList;
    }

}
