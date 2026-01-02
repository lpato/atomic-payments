package com.lsp.atomic_payments.domain.common;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {

  public Money minus(Money other) {
    return new Money(amount.subtract(other.amount), currency);
  }

  public Money plus(Money plus) {
    return new Money(amount.add(plus.amount), currency);
  }

  @Override
  public String toString() {

    return amount + " " + currency.getCurrencyCode();
  }
}
