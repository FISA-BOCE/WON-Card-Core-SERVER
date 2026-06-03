package com.woorifisa.won_card_core_server.domain.reward.dto.request;

import com.woorifisa.won_card_core_server.domain.reward.model.enums.SweepStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RewardSweepResultRequest(
        @NotNull
        Long sweepRequestId,

        @NotNull
        Long sweepExecutionId,

        @NotBlank
        String correlationId,

        @NotBlank
        String idempotencyKey,

        @NotNull
        SweepStatus resultStatus,

        String sweepFailureCode,

        String sweepFailureMessage
) {
}
