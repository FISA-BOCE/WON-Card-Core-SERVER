package com.woorifisa.won_card_core_server.domain.reward.dto.result;

public record RewardSweepChunkReservationResult(
        int reservedCount,
        Long lastProcessedPointLedgerId
) {
}
