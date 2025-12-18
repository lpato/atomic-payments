package com.lsp.atomic_payments.domain.account;

import java.util.UUID;

public record AccountId(
        UUID value) {

    public static AccountId newId() {
        return new AccountId(UUID.randomUUID());
    }

}
