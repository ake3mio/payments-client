package com.ake3m.payments.client.domain.entity;

import java.math.BigDecimal;

public record TransactionResultEntity(
        String id,
        String serverGeneratedId,
        TransactionResultStatus status,
        BigDecimal fee,
        String details) {}
