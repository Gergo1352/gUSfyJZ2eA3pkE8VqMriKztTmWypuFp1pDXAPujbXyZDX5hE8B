package com.gergelyivanics.metrics.service.dto;

public record MetricQueryResult(
    String sensorId,

    String metric,

    Double value
) {
}
