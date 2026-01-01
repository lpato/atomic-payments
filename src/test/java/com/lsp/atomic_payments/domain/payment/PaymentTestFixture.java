package com.lsp.atomic_payments.domain.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.common.Money;

public final class PaymentTestFixture {

    private PaymentTestFixture() {
    }

    public static Payment validPayment() {
        return new Payment(
                PaymentId.newId(),
                AccountId.newId(),
                AccountId.newId(),
                new Money(
                        BigDecimal.valueOf(5),
                        Currency.getInstance("EUR")),
                PaymentStatus.PENDING,
                "refernce",
                Instant.now());
    }
}
