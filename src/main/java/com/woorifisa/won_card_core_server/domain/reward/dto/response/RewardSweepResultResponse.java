package com.woorifisa.won_card_core_server.domain.reward.dto.response;

import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;

public record RewardSweepResultResponse(
        Long pointLedgerId,
        String sweepStatus
) {
    public static RewardSweepResultResponse from(CardPointLedger pointLedger) {
        return new RewardSweepResultResponse(
                pointLedger.getPointLedgerId(),
                pointLedger.getSweepStatus().name()
        );
    }
}
