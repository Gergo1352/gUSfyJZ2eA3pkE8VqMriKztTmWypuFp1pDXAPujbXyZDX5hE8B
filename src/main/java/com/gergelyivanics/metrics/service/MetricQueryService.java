package com.gergelyivanics.metrics.service;

import com.gergelyivanics.metrics.repository.MetricsQueryRepository;
import com.gergelyivanics.metrics.service.dto.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class MetricQueryService {

    private static final Duration MIN_RANGE = Duration.ofDays(1);
    private static final Duration MAX_RANGE = Duration.ofDays(31);
    private static final Duration DEFAULT_RANGE = Duration.ofDays(1);

    private final MetricsQueryRepository repository;

    public MetricQueryService(MetricsQueryRepository repository) {
        this.repository = repository;
    }

    public MetricQueryResponse queryMetrics(MetricQueryRequest query) {
        if (query.startDate() == null && query.endDate() == null) {
            List<MetricQueryResult> metrics = repository.queryLatest(query.sensorIds(), query.metrics());
            return new MetricQueryResponse(QueryResultType.LATEST, null, metrics);
        }

        validateAggregation(query.aggregation());

        Instant endDate = query.endDate() != null ? query.endDate() : Instant.now();
        Instant startDate = query.startDate() != null ? query.startDate() : endDate.minus(DEFAULT_RANGE);
        validateRange(startDate, endDate);

        List<MetricQueryResult> metrics = repository.query(
            query.sensorIds(),
            query.metrics(),
            query.aggregation(),
            startDate,
            endDate
        );

        return new MetricQueryResponse(
            QueryResultType.AGGREGATE,
            query.aggregation(),
            metrics
        );
    }

    private void validateRange(Instant startDate, Instant endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }

        Duration range = Duration.between(startDate, endDate);
        if (range.compareTo(MIN_RANGE) < 0) {
            throw new IllegalArgumentException("date range must be at least 1 day");
        }

        if (range.compareTo(MAX_RANGE) > 0) {
            throw new IllegalArgumentException("date range must not exceed 31 days");
        }
    }

    private void validateAggregation(Aggregation aggregation) {
        if (aggregation == null) {
            throw new IllegalArgumentException("aggregation is required when a date range is specified");
        }
    }
}
