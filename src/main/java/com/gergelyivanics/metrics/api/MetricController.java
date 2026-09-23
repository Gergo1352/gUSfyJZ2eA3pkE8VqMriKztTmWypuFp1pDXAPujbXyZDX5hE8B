package com.gergelyivanics.metrics.api;

import com.gergelyivanics.metrics.service.MetricQueryService;
import com.gergelyivanics.metrics.service.MetricService;
import com.gergelyivanics.metrics.service.dto.MetricQueryRequest;
import com.gergelyivanics.metrics.service.dto.MetricQueryResponse;
import com.gergelyivanics.metrics.service.dto.MetricRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
@Validated
public class MetricController {

    private final MetricService metricService;

    private final MetricQueryService metricQueryService;

    public MetricController(MetricService metricService, MetricQueryService metricQueryService) {
        this.metricService = metricService;
        this.metricQueryService = metricQueryService;
    }

    @PostMapping("/sensors/{sensorId}")
    public ResponseEntity<Void> recordMetrics(
        @PathVariable String sensorId,
        @RequestBody @Size(min = 1, message = "at least one metric is required")
        List<@Valid MetricRequest> metrics
    ) {
        metricService.recordMetrics(sensorId, metrics);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/query")
    public MetricQueryResponse query(@RequestBody @Valid MetricQueryRequest query) {
        return metricQueryService.queryMetrics(query);
    }
}
