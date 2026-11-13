package com.rssfeedgenerator.exception;

import java.util.List;

public class FeedValidationException extends RuntimeException {

    private final List<String> errors;

    public FeedValidationException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
