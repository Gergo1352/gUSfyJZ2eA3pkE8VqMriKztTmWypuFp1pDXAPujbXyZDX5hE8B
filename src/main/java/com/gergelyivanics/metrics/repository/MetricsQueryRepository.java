package com.gergelyivanics.metrics.repository;

import com.gergelyivanics.metrics.service.dto.*;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static com.example.jooq.Tables.SENSOR_METRIC;
import static org.jooq.impl.DSL.*;

@Repository
public class MetricsQueryRepository {

    private final DSLContext dslContext;

    public MetricsQueryRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<MetricQueryResult> query(List<String> sensorIds, List<Metric> metrics,
                                         Aggregation aggregation, Instant startDate, Instant endDate) {
        Field<Double> aggregatedValue = applyAggregation(SENSOR_METRIC.VALUE, aggregation);
        var condition = buildCondition(sensorIds, getMetricNames(metrics), startDate, endDate);

        return dslContext
            .select(SENSOR_METRIC.SENSOR_ID, SENSOR_METRIC.METRIC, aggregatedValue)
            .from(SENSOR_METRIC)
            .where(condition)
            .groupBy(SENSOR_METRIC.SENSOR_ID, SENSOR_METRIC.METRIC)
            .orderBy(SENSOR_METRIC.SENSOR_ID, SENSOR_METRIC.METRIC)
            .fetch(record -> new MetricQueryResult(
                record.get(SENSOR_METRIC.SENSOR_ID),
                record.get(SENSOR_METRIC.METRIC),
                record.get(aggregatedValue)
            ));
    }

    private Condition buildCondition(List<String> sensorIds, List<String> metrics, Instant startDate, Instant endDate) {
        Condition condition = trueCondition()
            .and(SENSOR_METRIC.RECORDED_AT.ge(startDate.atOffset(ZoneOffset.UTC)))
            .and(SENSOR_METRIC.RECORDED_AT.lt(endDate.atOffset(ZoneOffset.UTC)))
            .and(SENSOR_METRIC.METRIC.in(metrics));

        if (sensorIds != null && !sensorIds.isEmpty()) {
            condition = condition.and(SENSOR_METRIC.SENSOR_ID.in(sensorIds));
        }

        return condition;
    }

    private Field<Double> applyAggregation(Field<Double> value, Aggregation aggregation) {
        return switch (aggregation) {
            case MIN -> min(value);
            case MAX -> max(value);
            case SUM -> sum(value).cast(Double.class);
            case AVG -> avg(value).cast(Double.class);
        };
    }

    public List<MetricQueryResult> queryLatest(List<String> sensorIds, List<Metric> metrics) {
        var criteria = SENSOR_METRIC.METRIC.in(getMetricNames(metrics));

        if (sensorIds != null && !sensorIds.isEmpty()) {
            criteria = criteria.and(SENSOR_METRIC.SENSOR_ID.in(sensorIds));
        }

        return dslContext
            .select(
                SENSOR_METRIC.SENSOR_ID,
                SENSOR_METRIC.METRIC,
                SENSOR_METRIC.VALUE
            )
            .distinctOn(
                SENSOR_METRIC.SENSOR_ID,
                SENSOR_METRIC.METRIC
            )
            .from(SENSOR_METRIC)
            .where(criteria)
            .orderBy(
                SENSOR_METRIC.SENSOR_ID,
                SENSOR_METRIC.METRIC,
                SENSOR_METRIC.RECORDED_AT.desc(),
                SENSOR_METRIC.ID.desc()
            )
            .fetch(record -> new MetricQueryResult(
                record.get(SENSOR_METRIC.SENSOR_ID),
                record.get(SENSOR_METRIC.METRIC),
                record.get(SENSOR_METRIC.VALUE)
            ));
    }

    private List<String> getMetricNames(List<Metric> metrics) {
        return metrics.stream()
            .map(Metric::name)
            .toList();
    }
}
