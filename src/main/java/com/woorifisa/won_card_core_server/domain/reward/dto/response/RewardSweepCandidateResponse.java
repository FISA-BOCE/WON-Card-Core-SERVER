package com.woorifisa.won_card_core_server.domain.reward.dto.response;

import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;

import java.util.List;
import java.util.UUID;

public record RewardSweepCandidateResponse(
        String baseMonth,
        List<RewardSweepCandidateItem> candidates
) {
    public record RewardSweepCandidateItem(
            Long pointLedgerId,
            UUID cardUserUuid,
            Long performanceId,
            String baseMonth,
            Long pointAmount,
            Long krwAmount
    ) {

        public static RewardSweepCandidateItem from(
                CardPointLedger pointLedger,
                Long amount
        ) {
            return new RewardSweepCandidateItem(
                    pointLedger.getPointLedgerId(),
                    pointLedger.getCardUserUuid(),
                    pointLedger.getPerformanceId(),
                    pointLedger.getBaseMonth(),
                    amount,
                    amount
            );
        }
    }
}
