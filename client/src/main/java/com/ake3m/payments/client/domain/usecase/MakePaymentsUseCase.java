package com.ake3m.payments.client.domain.usecase;

import com.ake3m.payments.client.domain.entity.PaymentRequestEntity;
import com.ake3m.payments.client.domain.entity.TransactionResultsEntity;
import com.ake3m.payments.client.domain.ports.PaymentGatewayPort;
import com.ake3m.payments.client.domain.ports.PaymentTransactionsPort;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public class MakePaymentsUseCase {
    private final PaymentGatewayPort paymentGateway;
    private final PaymentTransactionsPort paymentTransactions;

    public MakePaymentsUseCase(PaymentGatewayPort paymentGateway,
                               PaymentTransactionsPort paymentTransactions) {
        this.paymentGateway = paymentGateway;
        this.paymentTransactions = paymentTransactions;
    }

    public CompletableFuture<TransactionResultsEntity> pay(List<PaymentRequestEntity> payments) {
        return paymentGateway.pay(payments)
                             .thenCompose(paymentTransactions::getResults);
    }
}
