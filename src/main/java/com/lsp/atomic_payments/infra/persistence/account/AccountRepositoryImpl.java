package com.lsp.atomic_payments.infra.persistence.account;

import org.springframework.stereotype.Repository;

import com.lsp.atomic_payments.domain.account.Account;
import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.account.AccountRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

    private final AccountMapper accountMapper;
    private final AccountR2dbcRepository accountR2dbcRepository;

    @Override
    public Mono<Account> findById(AccountId accountId) {

        return accountR2dbcRepository.findById(accountId.value()).map(accountMapper::toDomain);
    }

    @Override
    public Mono<Account> save(Account account) {

        return accountR2dbcRepository.save(accountMapper.toEntity(account)).map(accountMapper::toDomain);
    }

    @Override
    public Flux<Account> findAll() {

        return accountR2dbcRepository.findAll().map(accountMapper::toDomain);
    }

    @Override
    public Mono<Account> update(Account account) {
        AccountEntity accountEntity = accountMapper.toEntity(account);
        accountEntity.setNew(false);

        return accountR2dbcRepository.save(accountEntity).map(accountMapper::toDomain);

    }

}
