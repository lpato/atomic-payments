package com.lsp.atomic_payments.domain.exception;

import com.lsp.atomic_payments.domain.account.AccountId;

public class AccountNotActiveException extends DomainException {
    public AccountNotActiveException(AccountId accountId) {
        super(String.format("Account %s is not active", accountId));
    }
}
