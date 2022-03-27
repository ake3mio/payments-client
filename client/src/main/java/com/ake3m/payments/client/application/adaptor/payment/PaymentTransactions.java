package com.ake3m.payments.client.application.adaptor.payment;

import com.ake3m.payments.client.application.adaptor.dto.PaymentStatus;
import com.ake3m.payments.client.application.adaptor.dto.TransactionStatusResponse;
import com.ake3m.payments.client.domain.entity.PaymentReferenceEntity;
import com.ake3m.payments.client.domain.entity.TransactionResultEntity;
import com.ake3m.payments.client.domain.entity.TransactionResultStatus;
import com.ake3m.payments.client.domain.entity.TransactionResultsEntity;
import com.ake3m.payments.client.domain.ports.PaymentTransactionsPort;
import io.vertx.core.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class PaymentTransactions implements PaymentTransactionsPort {
    private final Vertx vertx;
    private final PaymentRestClient client;
    private final Queue<Map.Entry<List<PaymentReferenceEntity>, Promise<TransactionResultsEntity>>> queue = new LinkedList<>();
    private final Map<String, TransactionResultEntity> transactionResults = new HashMap<>();

    public PaymentTransactions(Vertx vertx, PaymentRestClient client) {
        this.vertx = vertx;
        this.client = client;
        process();
    }

    @Override
    public CompletableFuture<TransactionResultsEntity> getResults(List<PaymentReferenceEntity> paymentReferences) {
        var promise = Promise.<TransactionResultsEntity>promise();

        addFailedTransactionResults(paymentReferences);

        queue.offer(Map.entry(paymentReferences, promise));

        return promise.future().toCompletionStage().toCompletableFuture();
    }

    private void addFailedTransactionResults(List<PaymentReferenceEntity> paymentReferences) {
        paymentReferences.forEach(paymentReference -> {
            if (paymentReference.status() == TransactionResultStatus.FAILURE) {
                final var transactionResultEntity = new TransactionResultEntity(
                        paymentReference.reference(),
                        paymentReference.conversationID(),
                        paymentReference.status(),
                        null,
                        paymentReference.message()
                );
                transactionResults.put(paymentReference.reference(), transactionResultEntity);
            }
        });
    }

    private void process() {
        vertx.setTimer(200, aLong -> CompositeFuture.join(getUpdatedTransactionStatuses())
                                                .onComplete(unused -> process()));
    }

    private List<Future> getUpdatedTransactionStatuses() {
        return queue
                .stream()
                .map(this::getUpdatedTransactionStatuses)
                .map(Future.class::cast)
                .toList();
    }

    private Future<?> getUpdatedTransactionStatuses(Map.Entry<List<PaymentReferenceEntity>, Promise<TransactionResultsEntity>> entry) {
        final var referenceEntities = entry.getKey();
        final var results = selectTransactionResults(referenceEntities);

        if (hasProcessedAllTransactions(referenceEntities, results)) {
            return onAllProcessedTransactions(entry, results);
        }

        return CompositeFuture.join(getTransactionStatuses(referenceEntities));
    }

    private Future<Object> onAllProcessedTransactions(Map.Entry<List<PaymentReferenceEntity>, Promise<TransactionResultsEntity>> entry,
                                                      List<TransactionResultEntity> results) {
        final var referenceEntities = entry.getKey();
        final var promise = entry.getValue();

        try {
            promise.complete(new TransactionResultsEntity(results));
        } finally {
            clearTransactionResults(referenceEntities);
        }

        return Future.succeededFuture();
    }

    private void clearTransactionResults(List<PaymentReferenceEntity> referenceEntities) {
        referenceEntities.forEach(paymentReference -> transactionResults.remove(paymentReference.reference()));
    }

    private List<Future> getTransactionStatuses(List<PaymentReferenceEntity> referenceEntities) {
        return selectTransactionsWithoutResults(referenceEntities)
                .map(this::getStatus)
                .map(Future.class::cast)
                .toList();
    }

    private boolean hasProcessedAllTransactions(List<PaymentReferenceEntity> referenceEntities, List<TransactionResultEntity> results) {
        return results.size() == referenceEntities.size();
    }

    private Future<TransactionStatusResponse> getStatus(PaymentReferenceEntity referenceEntity) {
        return client.status(referenceEntity.conversationID())
                     .onSuccess(this::handleStatusRequestResponse)
                     .onFailure(handleStatusRequestFailure(referenceEntity));
    }

    private void handleStatusRequestResponse(TransactionStatusResponse transactionStatusResponse) {
        final var status = PaymentStatus.mapToEntity(transactionStatusResponse.getStatus());
        if (isProcessed(status)) {

            final TransactionResultEntity transactionResultEntity;
            if (status == TransactionResultStatus.FAILURE || status == TransactionResultStatus.UNKNOWN) {
                transactionResultEntity = new TransactionResultEntity(
                        transactionStatusResponse.getCustomerReference(),
                        transactionStatusResponse.getReference(),
                        status,
                        null,
                        transactionStatusResponse.getMessage()
                );
            } else {
                transactionResultEntity = new TransactionResultEntity(
                        transactionStatusResponse.getCustomerReference(),
                        transactionStatusResponse.getReference(),
                        status,
                        transactionStatusResponse.getFee(),
                        transactionStatusResponse.getMessage()
                );
            }

            transactionResults.put(transactionStatusResponse.getCustomerReference(), transactionResultEntity);
        }
    }

    private Handler<Throwable> handleStatusRequestFailure(PaymentReferenceEntity referenceEntity) {
        return throwable -> {
            final var transactionResultEntity = new TransactionResultEntity(
                    referenceEntity.reference(),
                    referenceEntity.conversationID(),
                    TransactionResultStatus.FAILURE,
                    null,
                    throwable.getMessage()
            );
            transactionResults.put(referenceEntity.conversationID(), transactionResultEntity);
        };
    }

    private Stream<PaymentReferenceEntity> selectTransactionsWithoutResults(List<PaymentReferenceEntity> referenceEntities) {
        return referenceEntities
                .stream()
                .filter(paymentReferenceEntity -> !hasTransactionResult(paymentReferenceEntity));
    }

    private List<TransactionResultEntity> selectTransactionResults(List<PaymentReferenceEntity> referenceEntities) {
        return referenceEntities
                .stream()
                .filter(this::hasTransactionResult)
                .map(this::selectTransactionResult)
                .toList();
    }

    private TransactionResultEntity selectTransactionResult(PaymentReferenceEntity paymentReferenceEntity) {
        return this.transactionResults.get(paymentReferenceEntity.reference());
    }

    private boolean hasTransactionResult(PaymentReferenceEntity paymentReferenceEntity) {
        return this.transactionResults.containsKey(paymentReferenceEntity.reference());
    }

    private boolean isProcessed(TransactionResultStatus status) {
        return status != null;
    }
}
