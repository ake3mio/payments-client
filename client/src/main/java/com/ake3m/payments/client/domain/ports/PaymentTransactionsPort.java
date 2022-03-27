package com.ake3m.payments.client.domain.ports;

import com.ake3m.payments.client.domain.entity.PaymentReferenceEntity;
import com.ake3m.payments.client.domain.entity.TransactionResultsEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PaymentTransactionsPort {
    CompletableFuture<TransactionResultsEntity> getResults(List<PaymentReferenceEntity> paymentReferences);
}
