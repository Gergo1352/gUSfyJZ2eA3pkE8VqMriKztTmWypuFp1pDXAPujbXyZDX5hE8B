package com.gergelyivanics.metrics.api;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorResponse handleConstraintValidation(ConstraintViolationException ex) {
        var errors = ex.getConstraintViolations()
            .stream()
            .map(error -> new ErrorResponse.FieldError(
                error.getPropertyPath().toString(),
                error.getMessage()
            ))
            .toList();

        return new ErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "Validation failed",
            errors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handleMessageNotReadable(
        HttpMessageNotReadableException ex
    ) {
        return new ErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ErrorResponse.FieldError(
                error.getField(),
                error.getDefaultMessage()
            ))
            .toList();

        return validationError(errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResponse handleIllegalArgumentException(
        IllegalArgumentException ex
    ) {
        return new ErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            null
        );
    }

    private ErrorResponse validationError(List<ErrorResponse.FieldError> errors) {
        return new ErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "Validation failed",
            errors
        );
    }
}
