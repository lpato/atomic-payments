package com.lsp.atomic_payments.domain.account;

import com.lsp.atomic_payments.domain.common.Money;
import java.time.Instant;

public record Account(
    AccountId accountId,
    String owner,
    Money balance,
    AccountStatus status,
    AccountVersion version,
    Instant createdAt) {

  public boolean isActive() {
    return AccountStatus.ACTIVE.equals(status);
  }

  public Account debit(Money amount) {

    Money newBalance = this.balance.minus(amount);

    return new Account(
        this.accountId, this.owner, newBalance, this.status, this.version, this.createdAt);
  }

  public Account credit(Money amount) {

    Money newBalance = this.balance.plus(amount);

    return new Account(
        this.accountId, this.owner, newBalance, this.status, this.version, this.createdAt);
  }
}
