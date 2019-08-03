package com.abed.app.ubertest.network.parser;

import android.support.annotation.Nullable;

import com.abed.app.ubertest.model.response.FlickrPhoto;
import com.abed.app.ubertest.model.response.FlickrSearchResponse;
import com.abed.app.ubertest.network.ValidationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FlickrSearchResponseParser extends FlickrResponseParser<FlickrSearchResponse> {

    private static final String KEY_PHOTOS_JSON = "photos";
    private static final String KEY_STAT = "stat";
    private static final String KEY_OK = "ok";
    private static final String KEY_PAGES = "pages";
    private static final String KEY_PHOTOS_ARRAY = "photo";
    private static final String KEY_PHOTO_ID = "id";
    private static final String KEY_SECRET = "secret";
    private static final String KEY_SERVER = "server";
    private static final String KEY_FARM = "farm";
    private static final String KEY_TITLE = "title";

    @Nullable
    @Override
    public FlickrSearchResponse parse(String response) throws ValidationException {
        FlickrSearchResponse flickrSearchResponse = null;

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString(KEY_STAT).contentEquals(KEY_OK)) {
                JSONObject resultObject = jsonObject.getJSONObject(KEY_PHOTOS_JSON);
                JSONArray jsonArray = resultObject.getJSONArray(KEY_PHOTOS_ARRAY);
                flickrSearchResponse = new FlickrSearchResponse(
                        resultObject.getInt(KEY_PAGES),
                        parseSearchArray(jsonArray)
                );
            }
        } catch (JSONException e) {
            throw new ValidationException("Invalid FlickrSearchResponse object", e);
        }

        return flickrSearchResponse;
    }


    private List<FlickrPhoto> parseSearchArray(JSONArray jsonArray) throws ValidationException {
        List<FlickrPhoto> flickrPhotoList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                FlickrPhoto flickrPhoto = new FlickrPhoto(
                        jsonObj.getString(KEY_PHOTO_ID),
                        jsonObj.getString(KEY_SECRET),
                        jsonObj.getString(KEY_SERVER),
                        jsonObj.getInt(KEY_FARM),
                        jsonObj.getString(KEY_TITLE));
                flickrPhotoList.add(flickrPhoto);
            }
        } catch (JSONException e) {
            throw new ValidationException("Invalid FlickrPhoto object", e);
        }

        return flickrPhotoList;
    }
}
