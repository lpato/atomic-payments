package com.lsp.atomic_payments.domain.exception;

import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.common.Money;

public class InsufficientFundsException extends DomainException {

  public InsufficientFundsException(AccountId accountId, Money amount) {
    super(String.format("Account %s has insufficient funds for %s payment", accountId, amount));
  }
}
