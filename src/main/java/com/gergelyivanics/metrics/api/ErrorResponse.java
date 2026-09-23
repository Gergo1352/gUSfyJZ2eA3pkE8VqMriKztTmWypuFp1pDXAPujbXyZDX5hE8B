package com.gergelyivanics.metrics.api;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
    Instant timestamp,

    int status,

    String error,

    String message,

    List<FieldError> errors
) {
    public record FieldError(
        String field,

        String message
    ) {
    }
}
