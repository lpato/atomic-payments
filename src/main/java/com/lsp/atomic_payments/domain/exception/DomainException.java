package com.lsp.atomic_payments.domain.exception;

public abstract class DomainException extends RuntimeException {
  protected DomainException(String message) {
    super(message);
  }
}
