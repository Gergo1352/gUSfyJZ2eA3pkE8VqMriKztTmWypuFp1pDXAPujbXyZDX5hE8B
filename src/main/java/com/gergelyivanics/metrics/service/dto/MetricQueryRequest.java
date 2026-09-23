package com.gergelyivanics.metrics.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.List;

public record MetricQueryRequest(
    List<String> sensorIds,

    @NotEmpty(message = "at least one metric is required")
    List<@Valid Metric> metrics,

    Aggregation aggregation,

    Instant startDate,

    Instant endDate
) {
}
