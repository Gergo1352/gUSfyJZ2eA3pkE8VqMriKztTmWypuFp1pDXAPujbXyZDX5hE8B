package com.gergelyivanics.metrics.repository;

import com.example.jooq.tables.SensorMetric;
import com.example.jooq.tables.records.SensorMetricRecord;
import com.gergelyivanics.metrics.service.dto.Metric;
import com.gergelyivanics.metrics.service.dto.MetricRequest;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class MetricsRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES =
        new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private MetricsRepository metricsRepository;

    @Autowired
    private DSLContext dslContext;

    private static final SensorMetric SENSOR_METRIC = SensorMetric.SENSOR_METRIC;

    @BeforeEach
    void setUp() {
        dslContext.deleteFrom(SENSOR_METRIC).execute();
    }

    @Test
    void saveAllMetrics() {
        var sensorId = "sensor-123";

        var recordedAt1 = Instant.parse("2026-09-22T10:00:00Z");
        var recordedAt2 = Instant.parse("2026-09-22T10:01:00Z");

        var metrics = List.of(
            new MetricRequest(
                /* metric */ Metric.TEMPERATURE,
                /* value */ 21.5,
                /* recordedAt */ recordedAt1
            ),
            new MetricRequest(
                Metric.HUMIDITY,
                55.2,
                recordedAt2
            )
        );

        metricsRepository.saveAll(sensorId, metrics);

        List<SensorMetricRecord> records = dslContext
            .selectFrom(SENSOR_METRIC)
            .orderBy(SENSOR_METRIC.RECORDED_AT.asc())
            .fetch();

        assertThat(records).hasSize(2);

        assertThat(records.get(0).getSensorId())
            .isEqualTo(sensorId);
        assertThat(records.get(0).getMetric())
            .isEqualTo("TEMPERATURE");
        assertThat(records.get(0).getValue())
            .isEqualTo(21.5);
        assertThat(records.get(0).getRecordedAt().toInstant())
            .isEqualTo(recordedAt1);

        assertThat(records.get(1).getSensorId())
            .isEqualTo(sensorId);
        assertThat(records.get(1).getMetric())
            .isEqualTo("HUMIDITY");
        assertThat(records.get(1).getValue())
            .isEqualTo(55.2);
        assertThat(records.get(1).getRecordedAt().toInstant())
            .isEqualTo(recordedAt2);
    }
}
