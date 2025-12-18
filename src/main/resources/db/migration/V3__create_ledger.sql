CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    payment_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    entry_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_ledger_account FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT fk_ledger_payment FOREIGN KEY (payment_id) REFERENCES payments (id)
)