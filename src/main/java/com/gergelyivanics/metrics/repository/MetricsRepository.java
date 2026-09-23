package com.gergelyivanics.metrics.repository;

import com.example.jooq.tables.records.SensorMetricRecord;
import com.gergelyivanics.metrics.service.dto.MetricRequest;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

@Repository
public class MetricsRepository {

    private final DSLContext dslContext;

    public MetricsRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public void saveAll(String sensorId, List<MetricRequest> metrics) {
        Instant now = Instant.now();

        List<SensorMetricRecord> records = metrics.stream()
            .map(request -> {
                var record = new SensorMetricRecord();

                record.setSensorId(sensorId);
                record.setMetric(request.metric().name());
                record.setValue(request.value());

                var recordedAt = request.recordedAt() != null ? request.recordedAt() : now;
                record.setRecordedAt(recordedAt.atOffset(ZoneOffset.UTC));

                return record;
            })
            .toList();

        dslContext.batchInsert(records).execute();
    }
}
