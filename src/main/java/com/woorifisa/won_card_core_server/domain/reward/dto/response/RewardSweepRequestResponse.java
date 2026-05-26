package com.woorifisa.won_card_core_server.domain.reward.dto.response;

import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;

public record RewardSweepRequestResponse(
        Long pointLedgerId,
        Long performanceId,
        Long pointAmount,
        Long krwAmount,
        String sweepStatus
) {

    public static RewardSweepRequestResponse from(CardPointLedger pointLedger, Long amount) {
        return new RewardSweepRequestResponse(
                pointLedger.getPointLedgerId(),
                pointLedger.getPerformanceId(),
                amount,
                amount,
                pointLedger.getSweepStatus().name()
        );
    }
}
