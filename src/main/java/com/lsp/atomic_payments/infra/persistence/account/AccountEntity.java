package com.lsp.atomic_payments.infra.persistence.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounts")
@Getter
@Setter
@RequiredArgsConstructor
public class AccountEntity implements Persistable<UUID> {

  @Id private UUID id;

  private String owner;

  @Column("balance_amount")
  private BigDecimal balanceAmount;

  @Column("balance_currency")
  private String balanceCurrency;

  private String status;

  @Version private Long version;

  @Column("created_at")
  Instant createdAt;

  @Transient private boolean isNew = true;

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public boolean isNew() {
    return isNew;
  }

  @PersistenceCreator
  public AccountEntity(
      UUID id,
      String owner,
      BigDecimal balanceAmount,
      String balanceCurrency,
      String status,
      Long version,
      Instant createdAt) {
    this.id = id;
    this.owner = owner;
    this.balanceAmount = balanceAmount;
    this.balanceCurrency = balanceCurrency;
    this.status = status;
    this.version = version;
    this.createdAt = createdAt;
    this.isNew = false;
  }

  public static AccountEntity newEntity(
      UUID id,
      String owner,
      BigDecimal balanceAmount,
      String balanceCurrency,
      String status,
      Long version) {

    AccountEntity accountEntity =
        new AccountEntity(
            id, owner, balanceAmount, balanceCurrency, status, version, Instant.now());
    accountEntity.isNew = true;

    return accountEntity;
  }
}
