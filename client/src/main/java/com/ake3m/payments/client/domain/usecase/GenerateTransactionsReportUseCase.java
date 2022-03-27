
package com.ake3m.payments.client.domain.usecase;

import com.ake3m.payments.client.domain.entity.TransactionResultsEntity;
import com.ake3m.payments.client.domain.exception.EmptyTransactionResultsException;
import com.ake3m.payments.client.domain.ports.TransactionReportGeneratorPort;

import java.util.concurrent.CompletableFuture;


public class GenerateTransactionsReportUseCase {
    private final TransactionReportGeneratorPort reportGenerator;

    public GenerateTransactionsReportUseCase(TransactionReportGeneratorPort reportGenerator) {
        this.reportGenerator = reportGenerator;
    }

    public CompletableFuture<Void> create(TransactionResultsEntity transactionResults) {
        if(transactionResults.results().isEmpty()) {
            return CompletableFuture.failedFuture(new EmptyTransactionResultsException());
        }
        return reportGenerator.write(transactionResults);
    }
}
