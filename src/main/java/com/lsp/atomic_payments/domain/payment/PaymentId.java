package com.lsp.atomic_payments.domain.payment;

import java.util.UUID;

public record PaymentId(UUID value) {

  public static PaymentId newId() {
    return new PaymentId(UUID.randomUUID());
  }
}
