package com.ake3m.payments.client.domain;

import com.ake3m.payments.client.domain.entity.PaymentReferenceEntity;
import com.ake3m.payments.client.domain.entity.TransactionResultsEntity;
import com.ake3m.payments.client.domain.ports.PaymentGatewayPort;
import com.ake3m.payments.client.domain.ports.PaymentTransactionsPort;
import com.ake3m.payments.client.domain.ports.TransactionReportGeneratorPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.ake3m.payments.TestData.PAYMENT_REQUEST;
import static com.ake3m.payments.TestData.TRANSACTION_RESULT_ENTITY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentGatewayPort paymentGateway;

    @Mock
    private TransactionReportGeneratorPort transactionReportGenerator;

    @Mock
    private PaymentTransactionsPort paymentTransactionsPort;


    @InjectMocks
    private PaymentService paymentService;

    @Test
    void payAndGenerateReport() {
        final var paymentReferenceEntities = List.of(
                new PaymentReferenceEntity(
                        "referenceA",
                        null,
                        null,
                        null
                )
        );

        final var transactionResultEntities = List.of(TRANSACTION_RESULT_ENTITY);
        final var transactionResultsEntity = new TransactionResultsEntity(transactionResultEntities);
        final var paymentRequests = List.of(PAYMENT_REQUEST);

        when(paymentGateway.pay(any())).thenReturn(CompletableFuture.completedFuture(paymentReferenceEntities));
        when(paymentTransactionsPort.getResults(any())).thenReturn(CompletableFuture.completedFuture(transactionResultsEntity));
        when(transactionReportGenerator.write(any())).thenReturn(CompletableFuture.completedFuture(null));

        paymentService.payAndGenerateReport(paymentRequests).join();

        var order = inOrder(paymentGateway, paymentTransactionsPort, transactionReportGenerator);
        order.verify(paymentGateway).pay(paymentRequests);
        order.verify(paymentTransactionsPort).getResults(paymentReferenceEntities);
        order.verify(transactionReportGenerator).write(transactionResultsEntity);
    }
}
