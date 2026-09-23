package com.gergelyivanics.metrics.service;

import com.gergelyivanics.metrics.repository.MetricsRepository;
import com.gergelyivanics.metrics.service.dto.Metric;
import com.gergelyivanics.metrics.service.dto.MetricRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MetricServiceTest {

    @Mock
    private MetricsRepository repository;

    private MetricService service;

    @BeforeEach
    void setUp() {
        service = new MetricService(repository);
    }

    @Test
    void recordMetrics() {
        var sensorId = "sensor-1";

        var metrics = List.of(
            new MetricRequest(
                Metric.TEMPERATURE,
                21.5,
                Instant.now().plus(Duration.ofMinutes(5))
            ),
            new MetricRequest(
                Metric.HUMIDITY,
                55.0,
                Instant.now().plus(Duration.ofMinutes(10))
            )
        );
        service.recordMetrics(sensorId, metrics);

        verify(repository).saveAll(sensorId, metrics);
    }
}
