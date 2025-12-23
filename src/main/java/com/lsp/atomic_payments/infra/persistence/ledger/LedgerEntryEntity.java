package com.lsp.atomic_payments.infra.persistence.ledger;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import lombok.Getter;
import lombok.Setter;

@Table("ledger_entries")
@Getter
@Setter
public class LedgerEntryEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("account_id")
    private UUID accountId;

    @Column("payment_id")
    private UUID paymentId;

    @Column("amount")
    private BigDecimal amount;

    @Column("currency")
    private String currency;

    @Column("entry_type")
    private String type;

    @Column("created_at")
    private Instant createdAt;

    @Transient
    private boolean isNew = true;

    public LedgerEntryEntity(UUID id, UUID accountId, UUID paymentId, BigDecimal amount, String currency, String type,
            Instant createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.currency = currency;
        this.type = type;
        this.createdAt = createdAt;
        this.isNew = false;
    }

    public static LedgerEntryEntity newEntity(UUID id, UUID accountId, UUID paymentId, BigDecimal amount,
            String currency, String type) {

        LedgerEntryEntity ledgerEntity = new LedgerEntryEntity(id, accountId, paymentId, amount, currency, type,
                Instant.now());
        ledgerEntity.isNew = true;
        return ledgerEntity;
    }

    public LedgerEntryEntity() {
    }

    @Override
    @Nullable
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

}
