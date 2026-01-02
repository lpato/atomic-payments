package com.lsp.atomic_payments.api.payment;

import com.lsp.atomic_payments.api.dto.ApiErrorResponse;
import com.lsp.atomic_payments.domain.exception.AccountNotActiveException;
import com.lsp.atomic_payments.domain.exception.ConcurrentAccountUpdateException;
import com.lsp.atomic_payments.domain.exception.CurrencyMismatchException;
import com.lsp.atomic_payments.domain.exception.InsufficientFundsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PaymentExceptionHandler {

  @ExceptionHandler(InsufficientFundsException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiErrorResponse insufficientFunds(InsufficientFundsException ex) {
    return ApiErrorResponse.of("INSUFFICIENT_FUNDS", ex.getMessage());
  }

  @ExceptionHandler(AccountNotActiveException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiErrorResponse accountInactive(AccountNotActiveException ex) {
    return ApiErrorResponse.of("ACCOUNT_NOT_ACTIVE", ex.getMessage());
  }

  @ExceptionHandler(CurrencyMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrorResponse currencyMismatch(CurrencyMismatchException ex) {
    return ApiErrorResponse.of("CURRENCY_MISMATCH", ex.getMessage());
  }

  @ExceptionHandler(ConcurrentAccountUpdateException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiErrorResponse concurrentUpdate(ConcurrentAccountUpdateException ex) {
    return ApiErrorResponse.of("CONCURRENT_UPDATE", ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiErrorResponse unexpected(Exception ex) {
    return ApiErrorResponse.of("INTERNAL_ERROR", "Unexpected error");
  }
}
