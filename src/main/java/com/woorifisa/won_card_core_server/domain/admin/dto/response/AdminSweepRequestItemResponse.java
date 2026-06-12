package com.woorifisa.won_card_core_server.domain.admin.dto.response;

import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.SweepStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminSweepRequestItemResponse(
        Long sweepRequestId,
        Long pointLedgerId,
        UUID cardUserUuid,
        String baseMonth,
        Long pointAmount,
        String sweepStatus,
        String failureCode,
        String failureMessage,
        LocalDateTime requestedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AdminSweepRequestItemResponse from(CardPointLedger ledger) {
        LocalDateTime completedAt = ledger.getSweepStatus() == SweepStatus.COMPLETED
                ? ledger.getUpdatedAt()
                : null;

        return new AdminSweepRequestItemResponse(
                ledger.getSweepRequestId(),
                ledger.getPointLedgerId(),
                ledger.getCardUserUuid(),
                ledger.getBaseMonth(),
                ledger.getDisplayPointAmount(),
                ledger.getSweepStatus().name(),
                ledger.getSweepFailureCode(),
                ledger.getSweepFailureMessage(),
                ledger.getSweepRequestedAt(),
                completedAt,
                ledger.getCreatedAt(),
                ledger.getUpdatedAt()
        );
    }
}
