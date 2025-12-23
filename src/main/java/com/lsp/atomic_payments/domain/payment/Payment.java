package com.lsp.atomic_payments.domain.payment;

import java.time.Instant;

import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.common.Money;

public record Payment(
                PaymentId paymentId,
                AccountId fromAccountId,
                AccountId toAccountId,
                Money amount,
                PaymentStatus status,
                String reference,
                Instant createdAt) {

        public static Payment initiate(PaymentCommand command) {
                return new Payment(PaymentId.newId(), command.fromAccountId(), command.toAccountId(), command.amount(),
                                PaymentStatus.PENDING, command.reference(), Instant.now());
        }

}
