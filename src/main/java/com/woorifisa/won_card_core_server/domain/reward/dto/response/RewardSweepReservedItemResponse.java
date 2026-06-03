package com.woorifisa.won_card_core_server.domain.reward.dto.response;

import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;

import java.time.LocalDateTime;
import java.util.UUID;

public record RewardSweepReservedItemResponse(
        Long sweepRequestId,
        String eventType,
        String eventId,
        String correlationId,
        String idempotencyKey,
        UUID cardUserUuid,
        Long performanceId,
        Long pointLedgerId,
        String baseMonth,
        Long pointAmount,
        Long krwAmount,
        LocalDateTime requestedAt
) {
    public static RewardSweepReservedItemResponse from(CardPointLedger ledger, Long pointAmount) {
        String eventId = "CARD-SWEEP-" + ledger.getSweepRequestId();
        String correlationId = "CARD-SWEEP-" + ledger.getPointLedgerId();

        return new RewardSweepReservedItemResponse(
                ledger.getSweepRequestId(),
                "SWEEP_REQUESTED",
                eventId,
                correlationId,
                ledger.getIdempotencyKey(),
                ledger.getCardUserUuid(),
                ledger.getPerformanceId(),
                ledger.getPointLedgerId(),
                ledger.getBaseMonth(),
                pointAmount,
                pointAmount,
                ledger.getSweepRequestedAt()
        );
    }
}
