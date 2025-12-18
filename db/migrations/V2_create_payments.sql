CREATE TABLE payments (
    id UUID PRIMARY KEY,
    from_account_id UUID NOT NULL,
    to_account_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reference VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_payment_from_account FOREING KEY (from_account_id) REFERENCES accounts (id),
    CONSTRAINT fk_payment_to_account FOREING KEY (to_account_id) REFERENCES accounts (id),
)