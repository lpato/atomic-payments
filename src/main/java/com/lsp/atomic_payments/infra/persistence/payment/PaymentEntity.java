package com.lsp.atomic_payments.infra.persistence.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter;
import lombok.Setter;

@Table("payments")
@Getter
@Setter
public class PaymentEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("from_account_id")
    private UUID fromAccountId;

    @Column("to_account_id")
    private UUID toAccountId;

    @Column("amount")
    private BigDecimal amount;

    @Column("currency")
    private String currency;

    private String status;

    private String reference;

    @Column("created_at")
    private Instant createdAt;

    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void markNotNew() {
        this.isNew = false;
    }

}
