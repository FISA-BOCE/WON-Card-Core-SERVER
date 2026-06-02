package com.woorifisa.won_card_core_server.domain.reward.dto.request;

import com.woorifisa.won_card_core_server.domain.reward.model.enums.SweepStatus;

public record RewardSweepResultRequest(
        Long sweepRequestId,
        Long sweepExecutionId,
        String correlationId,
        String idempotencyKey,
        SweepStatus resultStatus
) {
}
