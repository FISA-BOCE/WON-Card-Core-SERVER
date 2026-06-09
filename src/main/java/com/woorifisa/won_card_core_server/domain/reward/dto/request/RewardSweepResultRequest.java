package com.woorifisa.won_card_core_server.domain.reward.dto.request;

import com.woorifisa.won_card_core_server.domain.reward.model.enums.SweepStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

        @Size(max = 50)
        String sweepFailureCode,

        @Size(max = 500)
        String sweepFailureMessage
) {
}
