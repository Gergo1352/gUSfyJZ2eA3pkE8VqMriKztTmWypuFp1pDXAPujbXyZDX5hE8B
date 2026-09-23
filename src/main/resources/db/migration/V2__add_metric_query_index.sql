CREATE INDEX idx_sensor_metric_query
    ON sensor_metric ( sensor_id, metric, recorded_at DESC, id DESC )
    INCLUDE (value);

CREATE INDEX idx_sensor_metric_aggregation
    ON sensor_metric (recorded_at, sensor_id, metric)
    INCLUDE (value);
