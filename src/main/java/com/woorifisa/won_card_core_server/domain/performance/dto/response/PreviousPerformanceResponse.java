package com.woorifisa.won_card_core_server.domain.performance.dto.response;

import com.woorifisa.won_card_core_server.domain.performance.model.CardPerformance;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record PreviousPerformanceResponse(
        String baseMonth,
        String previousMonth,
        String rewardStatus,
        Long previousMonthSpendAmount,
        PerformanceDetail detail
) {

    public static PreviousPerformanceResponse from(
            CardPerformance performance,
            String previousMonth,
            String rewardStatus
    ) {
        Long previousMonthSpendAmount = toLong(performance.getPreviousMonthSpendAmount());

        return new PreviousPerformanceResponse(
                performance.getBaseMonth(),
                previousMonth,
                rewardStatus,
                previousMonthSpendAmount,
                new PerformanceDetail(
                        previousMonthSpendAmount,
                        toLong(performance.getRewardPointAmount())
                )
        );
    }

    private static Long toLong(BigDecimal amount) {
        if (amount == null) {
            return 0L;
        }

        return amount.setScale(0, RoundingMode.DOWN).longValue();
    }

    public record PerformanceDetail(
            Long totalSpendAmount,
            Long rewardPointAmount
    ) {
    }
}
