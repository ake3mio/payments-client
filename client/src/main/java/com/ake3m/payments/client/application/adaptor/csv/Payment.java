package com.ake3m.payments.client.application.adaptor.csv;

import com.ake3m.payments.client.domain.entity.PaymentRequestEntity;
import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @CsvBindByName(column = "ID", required = true)
    private String id;

    @CsvBindByName(column = "Recipient", required = true)
    private String recipient;

    @CsvBindByName(column = "Amount", required = true)
    private String amount;

    @CsvBindByName(column = "Currency", required = true)
    private String currency;


    public static PaymentRequestEntity mapToEntity(Payment payment) {
        return new PaymentRequestEntity(
                payment.recipient,
                payment.amount,
                payment.currency,
                payment.id);
    }

    public static List<PaymentRequestEntity> mapToEntities(List<Payment> payments) {
        return payments.stream().map(Payment::mapToEntity).toList();
    }

}
