package com.ake3m.payments.client.application.adaptor.payment;

import com.ake3m.payments.client.application.adaptor.dto.PaymentRequest;
import com.ake3m.payments.client.application.adaptor.dto.PaymentResponse;
import com.ake3m.payments.client.application.adaptor.dto.PaymentStatus;
import com.ake3m.payments.client.domain.entity.PaymentReferenceEntity;
import com.ake3m.payments.client.domain.entity.TransactionResultStatus;
import io.vertx.core.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.ake3m.payments.TestData.PAYMENT_REQUEST;
import static com.ake3m.payments.TestData.PAYMENT_REQUEST_TWO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayTest {

    @Mock
    PaymentRestClient client;

    @InjectMocks
    PaymentGateway paymentGateway;

    @Test
    void pay() {

        final var paymentResponse = PaymentResponse.builder()
                                                   .conversationID("conversationIDA")
                                                   .message("pending")
                                                   .status(PaymentStatus.TRANSACTION_PENDING)
                                                   .build();

        when(client.pay(PaymentRequest.mapToDto(PAYMENT_REQUEST)))
                .thenReturn(Future.succeededFuture(paymentResponse));

        when(client.pay(PaymentRequest.mapToDto(PAYMENT_REQUEST_TWO)))
                .thenReturn(Future.failedFuture("failed request"));

        final var results = paymentGateway.pay(List.of(PAYMENT_REQUEST, PAYMENT_REQUEST_TWO)).join();

        assertThat(results).containsExactly(
                new PaymentReferenceEntity(
                        PAYMENT_REQUEST.reference(),
                        paymentResponse.getConversationID(),
                        paymentResponse.getMessage(),
                        null
                ),
                new PaymentReferenceEntity(
                        PAYMENT_REQUEST_TWO.reference(),
                        null,
                        "failed request",
                        TransactionResultStatus.FAILURE
                )
        );
    }
}
