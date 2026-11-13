package com.rssfeedgenerator.exception;

public class FeedNotFoundException extends RuntimeException {

    public FeedNotFoundException(Long id) {
        super("Feed with id " + id + " was not found.");
    }
}
