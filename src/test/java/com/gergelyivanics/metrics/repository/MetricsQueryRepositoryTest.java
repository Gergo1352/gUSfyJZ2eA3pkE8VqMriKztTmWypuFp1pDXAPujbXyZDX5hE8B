package com.gergelyivanics.metrics.repository;

import com.example.jooq.tables.SensorMetric;
import com.gergelyivanics.metrics.service.dto.Aggregation;
import com.gergelyivanics.metrics.service.dto.Metric;
import com.gergelyivanics.metrics.service.dto.MetricQueryResult;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class MetricsQueryRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES =
        new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private MetricsQueryRepository repository;

    @Autowired
    private DSLContext dslContext;

    private static final SensorMetric SENSOR_METRIC =
        SensorMetric.SENSOR_METRIC;

    @BeforeEach
    void setUp() {
        dslContext.deleteFrom(SENSOR_METRIC).execute();
    }

    @Test
    void querySumForMultipleSensorsAndMetrics() {
        var start = Instant.now().minus(Duration.ofHours(1));
        var end = Instant.now();

        insert("sensor-1", Metric.TEMPERATURE, 10.0,
            start.plus(Duration.ofMinutes(10)));
        insert("sensor-1", Metric.TEMPERATURE, 20.0,
            start.plus(Duration.ofMinutes(20)));
        insert("sensor-1", Metric.HUMIDITY, 50.0,
            start.plus(Duration.ofMinutes(30)));

        insert("sensor-2", Metric.TEMPERATURE, 30.0,
            start.plus(Duration.ofMinutes(40)));

        var result = repository.query(
            List.of("sensor-1", "sensor-2"),
            List.of(Metric.TEMPERATURE, Metric.HUMIDITY),
            Aggregation.SUM,
            start,
            end
        );

        assertThat(result)
            .containsExactly(
                new MetricQueryResult("sensor-1", "HUMIDITY", 50.0),
                new MetricQueryResult("sensor-1", "TEMPERATURE", 30.0),
                new MetricQueryResult("sensor-2", "TEMPERATURE", 30.0)
            );
    }

    @Test
    void queryMin() {
        var start = Instant.now().minus(Duration.ofHours(1));
        var end = Instant.now();

        insert("sensor-1", Metric.TEMPERATURE, 20.0,
            start.plus(Duration.ofMinutes(10)));
        insert("sensor-1", Metric.TEMPERATURE, 10.0,
            start.plus(Duration.ofMinutes(20)));
        insert("sensor-1", Metric.TEMPERATURE, 15.0,
            start.plus(Duration.ofMinutes(30)));

        var result = repository.query(
            List.of("sensor-1"),
            List.of(Metric.TEMPERATURE),
            Aggregation.MIN,
            start,
            end
        );

        assertThat(result)
            .containsExactly(
                new MetricQueryResult(
                    "sensor-1",
                    "TEMPERATURE",
                    10.0
                )
            );
    }

    @Test
    void queryMax() {
        var start = Instant.now().minus(Duration.ofHours(1));
        var end = Instant.now();

        insert("sensor-1", Metric.TEMPERATURE, 20.0,
            start.plus(Duration.ofMinutes(10)));
        insert("sensor-1", Metric.TEMPERATURE, 10.0,
            start.plus(Duration.ofMinutes(20)));
        insert("sensor-1", Metric.TEMPERATURE, 30.0,
            start.plus(Duration.ofMinutes(30)));

        var result = repository.query(
            List.of("sensor-1"),
            List.of(Metric.TEMPERATURE),
            Aggregation.MAX,
            start,
            end
        );

        assertThat(result)
            .containsExactly(
                new MetricQueryResult(
                    "sensor-1",
                    "TEMPERATURE",
                    30.0
                )
            );
    }

    @Test
    void queryAverage() {
        var start = Instant.now().minus(Duration.ofHours(1));
        var end = Instant.now();

        insert("sensor-1", Metric.TEMPERATURE, 10.0,
            start.plus(Duration.ofMinutes(10)));
        insert("sensor-1", Metric.TEMPERATURE, 20.0,
            start.plus(Duration.ofMinutes(20)));
        insert("sensor-1", Metric.TEMPERATURE, 30.0,
            start.plus(Duration.ofMinutes(30)));

        var result = repository.query(
            List.of("sensor-1"),
            List.of(Metric.TEMPERATURE),
            Aggregation.AVG,
            start,
            end
        );

        assertThat(result)
            .containsExactly(
                new MetricQueryResult(
                    "sensor-1",
                    "TEMPERATURE",
                    20.0
                )
            );
    }

    @Test
    void queryLatestValueForEachSensorAndMetric() {
        var start = Instant.now().minus(Duration.ofHours(1));
        var end = Instant.now();

        insert("sensor-1", Metric.TEMPERATURE, 10.0,
            start.plus(Duration.ofMinutes(10)));

        insert("sensor-1", Metric.TEMPERATURE, 20.0,
            start.plus(Duration.ofMinutes(20)));

        insert("sensor-1", Metric.HUMIDITY, 40.0,
            start.plus(Duration.ofMinutes(5)));

        insert("sensor-1", Metric.HUMIDITY, 50.0,
            start.plus(Duration.ofMinutes(15)));

        insert("sensor-2", Metric.TEMPERATURE, 30.0,
            start.plus(Duration.ofMinutes(30)));

        var result = repository.queryLatest(
            List.of("sensor-1", "sensor-2"),
            List.of(Metric.TEMPERATURE, Metric.HUMIDITY)
        );

        assertThat(result)
            .containsExactly(
                new MetricQueryResult(
                    "sensor-1",
                    "HUMIDITY",
                    50.0
                ),
                new MetricQueryResult(
                    "sensor-1",
                    "TEMPERATURE",
                    20.0
                ),
                new MetricQueryResult(
                    "sensor-2",
                    "TEMPERATURE",
                    30.0
                )
            );
    }

    private void insert(String sensorId, Metric metric, double value, Instant recordedAt) {
        dslContext.insertInto(SENSOR_METRIC)
            .set(SENSOR_METRIC.SENSOR_ID, sensorId)
            .set(SENSOR_METRIC.METRIC, metric.name())
            .set(SENSOR_METRIC.VALUE, value)
            .set(SENSOR_METRIC.RECORDED_AT, recordedAt.atOffset(java.time.ZoneOffset.UTC))
            .execute();
    }
}
