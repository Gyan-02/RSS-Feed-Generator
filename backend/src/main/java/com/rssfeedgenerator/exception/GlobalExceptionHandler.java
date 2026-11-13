package com.rssfeedgenerator.exception;

import com.rssfeedgenerator.dto.ApiError;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeedValidationException.class)
    public ResponseEntity<ApiError> handleValidation(FeedValidationException exception) {
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                exception.getErrors()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(FeedNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(FeedNotFoundException exception) {
        ApiError error = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "Feed not found",
                List.of(exception.getMessage())
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
