package com.gergelyivanics.metrics.service;

import com.gergelyivanics.metrics.repository.MetricsQueryRepository;
import com.gergelyivanics.metrics.service.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricQueryServiceTest {

    @Mock
    private MetricsQueryRepository repository;

    private MetricQueryService service;

    @BeforeEach
    void setUp() {
        service = new MetricQueryService(repository);
    }

    @Test
    void queryLatestWhenNoDatesAreSpecified() {
        var sensorIds = List.of("sensor-1");
        var metrics = List.of(Metric.TEMPERATURE);

        var repositoryResult = List.of(
            new MetricQueryResult("sensor-1", "TEMPERATURE", 22.5)
        );

        when(repository.queryLatest(sensorIds, metrics))
            .thenReturn(repositoryResult);

        var request = new MetricQueryRequest(
            sensorIds,
            metrics,
            null,
            null,
            null
        );

        var response = service.queryMetrics(request);

        assertThat(response)
            .isEqualTo(
                new MetricQueryResponse(
                    QueryResultType.LATEST,
                    null,
                    repositoryResult
                )
            );

        verify(repository).queryLatest(sensorIds, metrics);
    }

    @Test
    void returnAggregateResultForValidDateRange() {
        var sensorIds = List.of("sensor-1");
        var metrics = List.of(Metric.TEMPERATURE);

        var startDate = Instant.now().minus(Duration.ofDays(1));
        var endDate = Instant.now();

        var repositoryResult = List.of(
            new MetricQueryResult("sensor-1", "TEMPERATURE", 21.5)
        );

        when(repository.query(
            sensorIds,
            metrics,
            Aggregation.AVG,
            startDate,
            endDate
        )).thenReturn(repositoryResult);

        var request = new MetricQueryRequest(
            sensorIds,
            metrics,
            Aggregation.AVG,
            startDate,
            endDate
        );

        var response = service.queryMetrics(request);

        assertThat(response)
            .isEqualTo(
                new MetricQueryResponse(
                    QueryResultType.AGGREGATE,
                    Aggregation.AVG,
                    repositoryResult
                )
            );

        verify(repository).query(
            sensorIds,
            metrics,
            Aggregation.AVG,
            startDate,
            endDate
        );
    }

    @Test
    void rejectMissingAggregationWhenDateRangeIsSpecified() {
        var request = new MetricQueryRequest(
            List.of("sensor-1"),
            List.of(Metric.TEMPERATURE),
            null,
            Instant.now().minus(Duration.ofHours(1)),
            Instant.now()
        );

        assertThatThrownBy(() -> service.queryMetrics(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(
                "aggregation is required when a date range is specified"
            );

        verifyNoInteractions(repository);
    }
}
