package com.woorifisa.won_card_core_server.domain.spend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CurrentSpendAmountResponse(
        Boolean hasCurrentSpendAmount,
        String baseMonth,
        Long currentSpendAmount,
        BigDecimal currentRewardRate,
        String nextPerformanceStatus,
        Long amountRemainingUntilNextPerformance,
        BigDecimal nextRewardRate,
        ExpectedReward expectedReward
) {

    public static CurrentSpendAmountResponse found(
            String baseMonth,
            Long currentSpendAmount,
            BigDecimal currentRewardRate,
            String nextPerformanceStatus,
            Long amountRemainingUntilNextPerformance,
            BigDecimal nextRewardRate,
            ExpectedReward expectedReward
    ) {
        return new CurrentSpendAmountResponse(
                true,
                baseMonth,
                currentSpendAmount,
                currentRewardRate,
                nextPerformanceStatus,
                amountRemainingUntilNextPerformance,
                nextRewardRate,
                expectedReward
        );
    }

    public static CurrentSpendAmountResponse notFound() {
        return new CurrentSpendAmountResponse(
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public record ExpectedReward(
            Long targetSpendAmount,
            BigDecimal rewardRate,
            Long expectedRewardAmount
    ) {
    }
}
