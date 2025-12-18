package com.lsp.atomic_payments.infra.persistence.account;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface AccountR2dbcRepository
        extends ReactiveCrudRepository<AccountEntity, UUID> {

}
