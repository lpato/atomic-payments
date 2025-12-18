package com.lsp.atomic_payments.domain.account;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountRepository {

    Mono<Account> findById(AccountId accountId);

    Mono<Account> save(Account account);

    Flux<Account> findAll();

}
