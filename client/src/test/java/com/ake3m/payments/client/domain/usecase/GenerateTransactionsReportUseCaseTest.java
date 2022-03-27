package com.ake3m.payments.client.domain.usecase;

import com.ake3m.payments.client.domain.entity.TransactionResultsEntity;
import com.ake3m.payments.client.domain.exception.EmptyTransactionResultsException;
import com.ake3m.payments.client.domain.ports.TransactionReportGeneratorPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.ake3m.payments.TestData.TRANSACTION_RESULT_ENTITY;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateTransactionsReportUseCaseTest {

    @Mock
    TransactionReportGeneratorPort transactionReportGenerator;

    @InjectMocks
    GenerateTransactionsReportUseCase generateTransactionsReport;

    @Test
    void create() {
        when(transactionReportGenerator.write(any())).thenReturn(CompletableFuture.completedFuture(null));

        final var transactionResultsEntity = new TransactionResultsEntity(List.of(TRANSACTION_RESULT_ENTITY));

        generateTransactionsReport.create(transactionResultsEntity).join();

        verify(transactionReportGenerator).write(transactionResultsEntity);
    }

    @Test
    void createWithNoResults() {
        final var transactionResultsEntity = new TransactionResultsEntity(List.of());

        assertThatThrownBy(() -> generateTransactionsReport.create(transactionResultsEntity).join())
                .hasCause(new EmptyTransactionResultsException());

        verify(transactionReportGenerator, never()).write(transactionResultsEntity);
    }
}
