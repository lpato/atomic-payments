package com.lsp.atomic_payments.infra.persistence.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Table("accounts")
@Getter
@Setter
@RequiredArgsConstructor
public class AccountEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    private String owner;

    @Column("balance_amount")
    private BigDecimal balanceAmount;

    @Column("balance_currency")
    private String balanceCurrency;

    private String status;

    @Version
    private Long version;

    @Column("created_at")
    Instant createdAt;

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
