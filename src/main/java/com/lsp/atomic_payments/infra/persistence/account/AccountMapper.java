package com.lsp.atomic_payments.infra.persistence.account;

import java.util.Currency;

import org.springframework.stereotype.Component;

import com.lsp.atomic_payments.domain.account.Account;
import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.account.AccountStatus;
import com.lsp.atomic_payments.domain.account.AccountVersion;
import com.lsp.atomic_payments.domain.common.Money;

@Component
public class AccountMapper {

    public AccountEntity toEntity(Account account) {
        if (account == null) {
            return null;
        }

        AccountEntity entity = new AccountEntity();
        entity.setId(account.accountId().value());
        entity.setOwner(account.owner());
        entity.setBalanceAmount(account.balance().amount());
        entity.setBalanceCurrency(account.balance().currency().getCurrencyCode());
        entity.setStatus(account.status().name());
        entity.setVersion(account.version().value());
        entity.setCreatedAt(account.createdAt());

        return entity;
    }

    public Account toDomain(AccountEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Account(
                new AccountId(entity.getId()),
                entity.getOwner(),
                new Money(entity.getBalanceAmount(), Currency.getInstance(entity.getBalanceCurrency())),
                AccountStatus.valueOf(entity.getStatus()),
                new AccountVersion(entity.getVersion()),
                entity.getCreatedAt());
    }

}
