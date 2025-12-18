package com.lsp.atomic_payments.infra.persistence.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounts")
public class AccountEntity {

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

}
