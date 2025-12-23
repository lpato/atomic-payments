package com.lsp.atomic_payments.domain.account;

import java.time.Instant;

import com.lsp.atomic_payments.domain.common.Money;

public record Account(
                AccountId accountId,
                String owner,
                Money balance,
                AccountStatus status,
                Instant createdAt) {

        public boolean isActive() {
                return AccountStatus.ACTIVE.equals(status);
        }
}
