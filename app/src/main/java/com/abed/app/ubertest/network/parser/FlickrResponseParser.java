package com.abed.app.ubertest.network.parser;

import com.abed.app.ubertest.network.ValidationException;

public abstract class FlickrResponseParser<R> {

    public abstract R parse(String response) throws ValidationException;

}
