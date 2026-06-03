package com.woorifisa.won_card_core_server.domain.reward.dto.payload;

import java.util.UUID;

public record SweepRequestOutboxPayload(
        String eventType,
        String correlationId,
        String idempotencyKey,
        Long sweepRequestId,
        Long pointLedgerId,
        UUID cardUserUuid,
        Long performanceId,
        String baseMonth,
        Long pointAmount,
        Long krwAmount
) {}
