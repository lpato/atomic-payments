CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    owner VARCHAR(255) NOT NULL,
    balance_amount NUMERIC(19, 2) NOT NULL,
    balance_currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL
)