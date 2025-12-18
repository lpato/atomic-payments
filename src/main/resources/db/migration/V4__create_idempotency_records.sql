CREATE TABLE idempotency_records (
    idempotency_key VARCHAR(255) PRIMARY KEY,
    request_hash VARCHAR(64) NOT NULL,
    response_payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
)