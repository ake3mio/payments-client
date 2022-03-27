package com.ake3m.payments.client.application.adaptor.payment;

import com.ake3m.payments.client.application.adaptor.dto.PaymentResponse;
import com.ake3m.payments.client.application.adaptor.dto.PaymentStatus;
import com.ake3m.payments.client.application.adaptor.dto.TransactionStatusResponse;
import com.ake3m.payments.client.domain.entity.TransactionResultsEntity;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, VertxExtension.class})
class PaymentTransactionsTest {

    @Mock
    PaymentRestClient client;


    @Test
    void getResults(Vertx vertx) {
        PaymentTransactions paymentTransactions = new PaymentTransactions(vertx, client);
        final var paymentResponseOne = PaymentResponse.builder()
                                                      .conversationID("conversationIDA")
                                                      .reference("referenceA")
                                                      .message("pending")
                                                      .status(PaymentStatus.TRANSACTION_PENDING)
                                                      .build();
        final var paymentResponseTwo = PaymentResponse.builder()
                                                      .message("Failed")
                                                      .reference("referenceB")
                                                      .status(PaymentStatus.INVALID_REQUEST)
                                                      .build();
        final var paymentResponseThree = PaymentResponse.builder()
                                                        .conversationID("conversationIDC")
                                                        .message("pending")
                                                        .reference("referenceC")
                                                        .status(PaymentStatus.TRANSACTION_PENDING)
                                                        .build();

        final var transactionStatusResponseOne = TransactionStatusResponse.builder()
                                                                          .status(PaymentStatus.TRANSACTION_SUCCEEDED)
                                                                          .timestamp(Instant.now())
                                                                          .reference(paymentResponseOne.getConversationID())
                                                                          .message("messageA")
                                                                          .customerReference(paymentResponseOne.getReference())
                                                                          .fee(BigDecimal.TEN)
                                                                          .build();

        final var transactionStatusResponseTwo = TransactionStatusResponse.builder()
                                                                          .status(PaymentStatus.INVALID_REQUEST)
                                                                          .timestamp(Instant.now())
                                                                          .reference(null)
                                                                          .message(paymentResponseTwo.getMessage())
                                                                          .customerReference(paymentResponseTwo.getReference())
                                                                          .fee(null)
                                                                          .build();

        final var transactionStatusResponseThree = TransactionStatusResponse.builder()
                                                                            .status(PaymentStatus.TRANSACTION_SUCCEEDED)
                                                                            .timestamp(Instant.now())
                                                                            .reference(paymentResponseThree.getConversationID())
                                                                            .message("messageC")
                                                                            .customerReference(paymentResponseThree.getReference())
                                                                            .fee(BigDecimal.TEN)
                                                                            .build();

        when(client.status(paymentResponseOne.getConversationID()))
                .thenReturn(Future.succeededFuture(
                        TransactionStatusResponse.builder()
                                                 .status(PaymentStatus.TRANSACTION_PENDING)
                                                 .build()
                ))
                .thenReturn(Future.succeededFuture(
                        TransactionStatusResponse.builder().status(PaymentStatus.TRANSACTION_PENDING).build()
                ))
                .thenReturn(Future.succeededFuture(
                        transactionStatusResponseOne
                ));

        when(client.status(paymentResponseThree.getConversationID()))
                .thenReturn(Future.succeededFuture(
                        transactionStatusResponseThree
                ));

        final var paymentReferences = PaymentResponse.mapToEntities(List.of(paymentResponseOne, paymentResponseTwo, paymentResponseThree));

        final var results = paymentTransactions.getResults(paymentReferences).join();

        assertThat(results).isEqualTo(
                new TransactionResultsEntity(
                        List.of(
                                TransactionStatusResponse.mapToEntity(transactionStatusResponseOne),
                                TransactionStatusResponse.mapToEntity(transactionStatusResponseTwo),
                                TransactionStatusResponse.mapToEntity(transactionStatusResponseThree)
                        )
                )
        );
    }
}
