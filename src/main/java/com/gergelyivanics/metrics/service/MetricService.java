package com.gergelyivanics.metrics.service;

import com.gergelyivanics.metrics.service.dto.MetricRequest;
import com.gergelyivanics.metrics.repository.MetricsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetricService {

    private final MetricsRepository repository;

    public MetricService(MetricsRepository repository) {
        this.repository = repository;
    }

    public void recordMetrics(
        String sensorId,
        List<MetricRequest> metrics
    ) {
        repository.saveAll(sensorId, metrics);
    }
}
