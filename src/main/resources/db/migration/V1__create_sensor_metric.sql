CREATE TABLE sensor_metric
(
    id          BIGSERIAL PRIMARY KEY,
    sensor_id   VARCHAR(100) NOT NULL,
    metric      VARCHAR(100) NOT NULL,
    value       DOUBLE PRECISION NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL
);
