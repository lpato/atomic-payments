package com.lsp.atomic_payments.infra.persistence.idempotency;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("idempotency_records")
@Getter
@Setter
public class IdempotencyRecordEntity implements Persistable<String> {

  @Id private String idempotencyKey;

  @Column("request_hash")
  private String requestHash;

  @Column("response_payload")
  private String responsePayload;

  @Column("created_at")
  private Instant createdAt;

  @PersistenceCreator
  public IdempotencyRecordEntity(
      String idempotencyKey, String requestHash, String responsePayload, Instant createdAt) {
    this.idempotencyKey = idempotencyKey;
    this.requestHash = requestHash;
    this.responsePayload = responsePayload;
    this.createdAt = createdAt;
    this.isNew = false;
  }

  public IdempotencyRecordEntity(
      String idempotencyKey, String requestHash, String responsePayload) {
    this.idempotencyKey = idempotencyKey;
    this.requestHash = requestHash;
    this.responsePayload = responsePayload;
    this.createdAt = Instant.now();
    this.isNew = true;
  }

  @Transient private boolean isNew = true;

  @Override
  public String getId() {
    return idempotencyKey;
  }

  @Override
  public boolean isNew() {
    return isNew;
  }
}
