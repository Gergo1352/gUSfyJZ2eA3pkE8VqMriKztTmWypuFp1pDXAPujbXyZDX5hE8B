package com.gergelyivanics.metrics.service.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record MetricRequest(
    @NotNull(message = "metric is required")
    Metric metric,

    @NotNull(message = "value is required")
    Double value,

    Instant recordedAt
) {
}
