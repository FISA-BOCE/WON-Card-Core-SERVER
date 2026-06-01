package com.woorifisa.won_card_core_server.domain.reward.dto.request;

public record RewardSweepResultRequest(
        Long sweepRequestId,
        Long sweepExecutionId,
        String correlationId,
        String idempotencyKey,
        String resultStatus
) {
}
