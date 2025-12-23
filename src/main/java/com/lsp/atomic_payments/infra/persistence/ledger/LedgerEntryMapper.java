package com.lsp.atomic_payments.infra.persistence.ledger;

import java.util.Currency;

import org.springframework.stereotype.Component;

import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.common.Money;
import com.lsp.atomic_payments.domain.ledger.EntryType;
import com.lsp.atomic_payments.domain.ledger.LedgerEntry;
import com.lsp.atomic_payments.domain.ledger.LedgerEntryId;
import com.lsp.atomic_payments.domain.payment.PaymentId;

@Component
public class LedgerEntryMapper {

    public LedgerEntryEntity toEntity(LedgerEntry ledgerEntry) {

        if (ledgerEntry == null) {
            return null;
        }

        LedgerEntryEntity entity = new LedgerEntryEntity();
        entity.setId(ledgerEntry.ledgerEntryId().value());
        entity.setAccountId(ledgerEntry.accountId().value());
        entity.setPaymentId(ledgerEntry.paymentId().value());
        entity.setAmount(ledgerEntry.amount().amount());
        entity.setCurrency(ledgerEntry.amount().currency().getCurrencyCode());
        entity.setType(ledgerEntry.type().name().toUpperCase());
        entity.setCreatedAt(ledgerEntry.createdAt());

        return entity;
    }

    public LedgerEntry toDomain(LedgerEntryEntity entity) {

        if (entity == null) {
            return null;
        }

        return new LedgerEntry(
                new LedgerEntryId(entity.getId()),
                new AccountId(entity.getAccountId()),
                new PaymentId(entity.getPaymentId()),
                new Money(entity.getAmount(), Currency.getInstance(entity.getCurrency())),
                EntryType.valueOf(entity.getType()),
                entity.getCreatedAt());
    }

}
