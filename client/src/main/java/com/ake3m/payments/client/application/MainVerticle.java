package com.ake3m.payments.client.application;

import com.ake3m.payments.client.domain.PaymentService;
import com.ake3m.payments.client.application.adaptor.csv.CSVPaymentsReader;
import com.ake3m.payments.client.application.adaptor.csv.CSVTransactionReportWriter;
import com.ake3m.payments.client.application.adaptor.csv.Payment;
import com.ake3m.payments.client.application.adaptor.payment.PaymentRestClient;
import com.ake3m.payments.client.application.adaptor.payment.PaymentGateway;
import com.ake3m.payments.client.application.adaptor.payment.PaymentTransactions;
import com.ake3m.payments.client.application.config.Configuration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static io.vertx.core.Future.fromCompletionStage;

@Slf4j
public class MainVerticle extends AbstractVerticle {
    private final Configuration configuration;
    private final File fileInput;
    private final Path fileOutput;

    public MainVerticle(Configuration configuration, File fileInput, Path fileOutput) {
        this.configuration = configuration;
        this.fileInput = fileInput;
        this.fileOutput = fileOutput;
    }

    @Override
    public void start() {
        new CSVPaymentsReader(vertx, fileInput)
                .readPayments()
                .flatMap(this::payAndGenerateReport)
                .onSuccess(unused -> log.info("Payment processing complete"))
                .onFailure(throwable -> log.info("There was an issue processing payments", throwable))
                .onComplete(unused -> vertx.close());

    }

    private Future<Void> payAndGenerateReport(List<Payment> payments) {
        final var client = new PaymentRestClient(vertx, configuration.getPaymentRestClientConfig());
        final var paymentGateway = new PaymentGateway(client);
        final var paymentTransactions = new PaymentTransactions(vertx, client);
        final var reportGenerator = new CSVTransactionReportWriter(vertx, fileOutput);
        final var paymentManager = new PaymentService(paymentGateway, paymentTransactions, reportGenerator);
        return fromCompletionStage(paymentManager.payAndGenerateReport(Payment.mapToEntities(payments)));
    }
}
