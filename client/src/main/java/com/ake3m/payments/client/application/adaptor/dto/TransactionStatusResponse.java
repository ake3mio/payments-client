package com.ake3m.payments.client.application.adaptor.dto;

import com.ake3m.payments.client.domain.entity.TransactionResultEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Getter
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionStatusResponse {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private PaymentStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private Instant timestamp;
    private String reference;
    private String message;
    private String customerReference;
    private BigDecimal fee;

    public static TransactionResultEntity mapToEntity(TransactionStatusResponse dto) {
        return new TransactionResultEntity(
                dto.customerReference,
                dto.reference,
                PaymentStatus.mapToEntity(dto.status),
                dto.fee,
                dto.message
        );
    }

    public static TransactionStatusResponse mapToDto(JsonObject entries) {
        final var status = entries.getInteger("status");
        final var timestamp = entries.getString("timestamp");
        final var customerReference = entries.getString("customerReference");
        final var fee = entries.getString("fee");
        final var reference = entries.getString("reference");
        final var message = entries.getString("message");

        return TransactionStatusResponse.builder()
                                        .status(PaymentStatus.findByStatus(status))
                                        .reference(reference)
                                        .timestamp(mapToInstant(timestamp))
                                        .reference(reference)
                                        .message(message)
                                        .customerReference(customerReference)
                                        .fee(mapToBigDecimal(fee))
                                        .build();
    }

    private static BigDecimal mapToBigDecimal(String fee) {
        return Optional.ofNullable(fee).map(BigDecimal::new).orElse(null);
    }

    private static Instant mapToInstant(String value) {
        final var formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        final var zonedDateTime = ZonedDateTime.parse(value, formatter);
        return zonedDateTime.toInstant();
    }
}
