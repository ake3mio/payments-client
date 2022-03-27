package com.ake3m.payments.client.application.adaptor.dto;

import com.ake3m.payments.client.domain.entity.PaymentReferenceEntity;
import com.ake3m.payments.client.domain.entity.TransactionResultStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentResponse {
    private String reference;
    private String conversationID;
    private String message;
    private PaymentStatus status;

    public static PaymentReferenceEntity mapToEntity(PaymentResponse dto) {
        final var status = PaymentStatus.mapToEntity(dto.status);
        return new PaymentReferenceEntity(
                dto.reference,
                dto.conversationID,
                dto.message,
                status.equals(TransactionResultStatus.UNKNOWN) ? null : status
        );
    }

    public static List<PaymentReferenceEntity> mapToEntities(List<Object> objects) {
        return objects.stream().map(o -> PaymentResponse.mapToEntity((PaymentResponse) o)).toList();
    }

    public static PaymentResponse mapToDto(JsonObject entries) {
        final var status = entries.getInteger("status");
        final var conversationID = entries.getString("conversationID");
        final var message = entries.getString("message");
        final var reference = entries.getString("reference");

        return PaymentResponse.builder()
                              .status(PaymentStatus.findByStatus(status))
                              .reference(reference)
                              .conversationID(conversationID)
                              .message(message)
                              .build();
    }
}
