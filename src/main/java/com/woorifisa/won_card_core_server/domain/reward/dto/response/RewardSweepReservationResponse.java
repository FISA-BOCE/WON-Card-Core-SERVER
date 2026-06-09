package com.woorifisa.won_card_core_server.domain.reward.dto.response;

import com.woorifisa.won_card_core_server.domain.reward.dto.result.RewardSweepChunkReservationResult;
import com.woorifisa.won_card_core_server.domain.reward.model.RewardSweepBatchExecution;

import java.util.List;

public record RewardSweepReservationResponse(
        Long batchExecutionId,
        String baseMonth,
        String status,
        int reservedCount,
        Long lastProcessedPointLedgerId,
        List<RewardSweepReservedItemResponse> reservedItems
) {
    public static RewardSweepReservationResponse from(
            RewardSweepBatchExecution batch,
            RewardSweepChunkReservationResult result
    ) {
        return new RewardSweepReservationResponse(
                batch.getBatchExecutionId(),
                batch.getBaseMonth(),
                batch.getStatus().name(),
                result.reservedCount(),
                result.lastProcessedPointLedgerId(),
                result.reservedItems()
        );
    }
}
