package com.woorifisa.won_card_core_server.domain.reward.dto.response;

import com.woorifisa.won_card_core_server.domain.performance.model.CardPerformance;
import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;

public record RewardSweepRequestResponse(
        Long pointLedgerId,
        Long performanceId,
        String baseMonth,
        Long pointAmount,
        Long krwAmount,
        String sweepStatus
) {

    public static RewardSweepRequestResponse from(CardPointLedger pointLedger, CardPerformance performance, Long amount) {
        return new RewardSweepRequestResponse(
                pointLedger.getPointLedgerId(),
                pointLedger.getPerformanceId(),
                performance.getBaseMonth(),
                amount,
                amount,
                pointLedger.getSweepStatus().name()
        );
    }
}
