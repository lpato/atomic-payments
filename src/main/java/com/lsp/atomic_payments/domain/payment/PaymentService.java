package com.lsp.atomic_payments.domain.payment;

import com.lsp.atomic_payments.domain.account.AccountRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PaymentService {

    private PaymentRepository paymentRepository;
    private AccountRepository accountRepository;

}
