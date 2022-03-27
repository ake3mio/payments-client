package com.ake3m.payments.client.domain.ports;

import com.ake3m.payments.client.domain.entity.TransactionResultsEntity;

import java.util.concurrent.CompletableFuture;

public interface TransactionReportGeneratorPort {
    CompletableFuture<Void> write(TransactionResultsEntity transactionResults);
}
