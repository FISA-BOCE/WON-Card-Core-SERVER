package com.woorifisa.won_card_core_server.domain.reward.dto.result;

import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepReservedItemResponse;

import java.util.List;

public record RewardSweepChunkReservationResult(
        int reservedCount,
        Long lastProcessedPointLedgerId,
        List<RewardSweepReservedItemResponse> reservedItems
) {
}
