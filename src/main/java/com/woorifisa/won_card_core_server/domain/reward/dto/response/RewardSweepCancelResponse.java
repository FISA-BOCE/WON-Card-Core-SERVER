package com.woorifisa.won_card_core_server.domain.reward.dto.response;

import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;

public record RewardSweepCancelResponse(
        Long pointLedgerId,
        String sweepStatus
) {
    public static RewardSweepCancelResponse from(CardPointLedger cardPointLedger) {
        return new RewardSweepCancelResponse(
                cardPointLedger.getPointLedgerId(),
                cardPointLedger.getSweepStatus().name()
        );
    }
}
