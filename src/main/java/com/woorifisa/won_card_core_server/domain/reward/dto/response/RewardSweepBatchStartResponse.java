package com.woorifisa.won_card_core_server.domain.reward.dto.response;

import com.woorifisa.won_card_core_server.domain.reward.model.RewardSweepBatchExecution;

public record RewardSweepBatchStartResponse(
        Long batchExecutionId,
        String baseMonth,
        String status,
        long requestedCount
) {
    public static RewardSweepBatchStartResponse from(RewardSweepBatchExecution batch) {
        return new RewardSweepBatchStartResponse(
                batch.getBatchExecutionId(),
                batch.getBaseMonth(),
                batch.getStatus().name(),
                batch.getRequestedCount()
        );
    }
}
