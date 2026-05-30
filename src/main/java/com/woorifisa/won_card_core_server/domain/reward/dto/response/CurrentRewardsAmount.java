package com.woorifisa.won_card_core_server.domain.reward.dto.response;

import com.woorifisa.won_card_core_server.domain.performance.model.CardPerformance;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.RewardStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record CurrentRewardsAmount(
        String baseMonth,
        RewardStatus rewardStatus,
        Long previousMonthSpendAmount,
        Long rewardPointAmount,
        BigDecimal rewardRate,
        String performanceStatus
) {

    public static CurrentRewardsAmount from(
            CardPerformance performance,
            RewardStatus rewardStatus
    ) {
        Long previousMonthSpendAmount = toLong(performance.getPreviousMonthSpendAmount());

        return new CurrentRewardsAmount(
                performance.getBaseMonth(),
                rewardStatus,
                previousMonthSpendAmount,
                toLong(performance.getRewardPointAmount()),
                performance.getRewardRate(),
                performance.getPerformanceStatus()
        );
    }

    private static Long toLong(BigDecimal amount) {
        if (amount == null) {
            return 0L;
        }

        return amount.setScale(0, RoundingMode.DOWN).longValue();
    }

}
