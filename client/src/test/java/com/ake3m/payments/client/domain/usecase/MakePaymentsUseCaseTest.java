package com.ake3m.payments.client.domain.usecase;

import com.ake3m.payments.client.domain.entity.PaymentReferenceEntity;
import com.ake3m.payments.client.domain.entity.TransactionResultsEntity;
import com.ake3m.payments.client.domain.ports.PaymentGatewayPort;
import com.ake3m.payments.client.domain.ports.PaymentTransactionsPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.ake3m.payments.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MakePaymentsUseCaseTest {

    @Mock
    private PaymentGatewayPort paymentGatewayPort;

    @Mock
    private PaymentTransactionsPort paymentTransactionsPort;

    @InjectMocks
    private MakePaymentsUseCase makePaymentsUseCase;

    @Test
    void pay() {
        final var payments = List.of(PAYMENT_REQUEST, PAYMENT_REQUEST_TWO);
        final var expectedResults = new TransactionResultsEntity(
                List.of(TRANSACTION_RESULT_ENTITY, TRANSACTION_RESULT_ENTITY_TWO)
        );
        final var paymentReferences = List.of(
                new PaymentReferenceEntity("refa", null, null, null),
                new PaymentReferenceEntity("refa", null, null, null)
        );

        when(paymentGatewayPort.pay(payments))
                .thenReturn(CompletableFuture.completedFuture(paymentReferences));

        when(paymentTransactionsPort.getResults(paymentReferences))
                .thenReturn(CompletableFuture.completedFuture(expectedResults));

        assertThat(makePaymentsUseCase.pay(payments).join()).isEqualTo(expectedResults);
    }
}
