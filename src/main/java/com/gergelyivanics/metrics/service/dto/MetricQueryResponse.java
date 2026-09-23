package com.gergelyivanics.metrics.service.dto;

import java.util.List;

public record MetricQueryResponse(
    QueryResultType queryType,

    Aggregation aggregation,

    List<MetricQueryResult> metrics
) {
}
