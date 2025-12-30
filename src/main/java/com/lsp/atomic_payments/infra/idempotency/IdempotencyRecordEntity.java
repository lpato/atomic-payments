package com.lsp.atomic_payments.infra.idempotency;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter;
import lombok.Setter;

@Table("idempotency_record")
@Getter
@Setter
public class IdempotencyRecordEntity {

    @Id
    private String idempotencyKey;

    @Column("request_hash")
    private String requestHash;

    @Column("response_payload")
    private String responsePayload;

    @Column("created_at")
    private Instant createdAt;

    public IdempotencyRecordEntity(String idempotencyKey, String requestHash, String responsePayload,
            Instant createdAt) {
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.responsePayload = responsePayload;
        this.createdAt = createdAt;
    }

}
