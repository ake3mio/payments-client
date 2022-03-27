package com.ake3m.payments.client.domain;

import com.ake3m.payments.client.domain.entity.PaymentRequestEntity;
import com.ake3m.payments.client.domain.ports.PaymentGatewayPort;
import com.ake3m.payments.client.domain.ports.PaymentTransactionsPort;
import com.ake3m.payments.client.domain.ports.TransactionReportGeneratorPort;
import com.ake3m.payments.client.domain.usecase.GenerateTransactionsReportUseCase;
import com.ake3m.payments.client.domain.usecase.MakePaymentsUseCase;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PaymentService {
    private final MakePaymentsUseCase makePaymentsUseCase;
    private final GenerateTransactionsReportUseCase generateReportsUseCase;

    public PaymentService(
            PaymentGatewayPort paymentGateway,
            PaymentTransactionsPort paymentTransactions,
            TransactionReportGeneratorPort reportGenerator) {
        this.makePaymentsUseCase = new MakePaymentsUseCase(paymentGateway, paymentTransactions);
        this.generateReportsUseCase = new GenerateTransactionsReportUseCase(reportGenerator);
    }

    public CompletableFuture<Void> payAndGenerateReport(List<PaymentRequestEntity> payments) {
        return makePaymentsUseCase.pay(payments)
                                  .thenCompose(generateReportsUseCase::create);
    }
}
