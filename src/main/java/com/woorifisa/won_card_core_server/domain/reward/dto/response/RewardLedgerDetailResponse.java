package com.woorifisa.won_card_core_server.domain.reward.dto.response;

import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;

import java.time.LocalDateTime;

public record RewardLedgerDetailResponse(
        Long pointLedgerId,
        String baseMonth,
        String type,
        Long pointAmount,
        LocalDateTime occurredAt,
        Object detail
) {

    public static RewardLedgerDetailResponse from(
            CardPointLedger pointLedger,
            Object detail
    ) {
        return new RewardLedgerDetailResponse(
                pointLedger.getPointLedgerId(),
                pointLedger.getBaseMonth(),
                pointLedger.getRewardProcessStatus().name(),
                pointLedger.getDisplayPointAmount(),
                pointLedger.getOccurredAt(),
                detail
        );
    }

    public record EarnRewardDetail(
            Long previousMonthSpendAmount,
            Long targetSpendAmount
    ) {
    }

    public record NotAppliedRewardDetail(
            Long previousMonthSpendAmount,
            Long targetSpendAmount,
            Long shortfallAmount
    ) {
    }
}
