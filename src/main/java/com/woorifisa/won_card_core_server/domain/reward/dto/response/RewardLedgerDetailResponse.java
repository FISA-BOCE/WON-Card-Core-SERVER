package com.woorifisa.won_card_core_server.domain.reward.dto.response;

import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;

import java.time.LocalDateTime;

public record RewardLedgerDetailResponse(
        Long pointLedgerId,
        String baseMonth,
        String type,
        Long pointAmount,
        String sweepStatus,
        String sweepFailureCode,
        String sweepFailureMessage,
        LocalDateTime occurredAt,
        RewardDetail detail
) {

    public static RewardLedgerDetailResponse from(
            CardPointLedger pointLedger,
            Long pointAmount,
            RewardDetail detail
    ) {
        return new RewardLedgerDetailResponse(
                pointLedger.getPointLedgerId(),
                pointLedger.getBaseMonth(),
                pointLedger.getRewardProcessStatus().name(),
                pointAmount,
                pointLedger.getSweepStatus().name(),
                pointLedger.getSweepFailureCode(),
                pointLedger.getSweepFailureMessage(),
                pointLedger.getOccurredAt(),
                detail
        );
    }

    public record RewardDetail(
            Long previousMonthSpendAmount,
            Long targetSpendAmount,
            Long shortfallAmount
    ) {
    }
}
