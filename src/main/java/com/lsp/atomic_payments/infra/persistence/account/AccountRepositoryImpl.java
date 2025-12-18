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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }

    @Override
    public Mono<Account> save(Account account) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public Flux<Account> findAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

}
