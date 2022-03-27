package com.ake3m.payments.client.application.adaptor.csv;

import com.ake3m.payments.client.domain.entity.TransactionResultEntity;
import com.ake3m.payments.client.domain.entity.TransactionResultStatus;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @CsvBindByName(column = "ID")
    @CsvBindByPosition(position = 0, required = true)
    private String id;

    @CsvBindByName(column = "Server-generated ID")
    @CsvBindByPosition(position = 1)
    private String serverGeneratedId;

    @CsvBindByName(column = "Status")
    @CsvBindByPosition(position = 2, required = true)
    private String status;

    @CsvBindByName(column = "Fee")
    @CsvBindByPosition(position = 3)
    private String fee;

    @CsvBindByName(column = "Details")
    @CsvBindByPosition(position = 4)
    String details;

    public static Transaction mapToDto(TransactionResultEntity transactionResult) {
        return new Transaction(
                transactionResult.id(),
                Optional.ofNullable(transactionResult.serverGeneratedId()).orElse(""),
                Optional.ofNullable(transactionResult.status()).map(TransactionResultStatus::getValue).orElse(null),
                Optional.ofNullable(transactionResult.fee()).map(BigDecimal::toPlainString).orElse(""),
                transactionResult.details()
        );
    }

    public static List<Transaction> mapToDtos(List<TransactionResultEntity> transactionResults) {
        return transactionResults.stream().map(Transaction::mapToDto).toList();
    }

}
