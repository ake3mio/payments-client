package com.ake3m.payments.client.application.config;

import com.ake3m.payments.client.application.adaptor.payment.PaymentRestClient.PaymentRestClientConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class Configuration {
    PaymentRestClientConfig paymentRestClientConfig;
}
