package com.woorifisa.won_card_core_server.domain.reward.service;

import com.woorifisa.won_card_core_server.domain.card.repository.CardUserRepository;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.CurrentRewardsAmount;
import com.woorifisa.won_card_core_server.domain.reward.exception.code.RewardErrorCode;
import com.woorifisa.won_card_core_server.domain.performance.model.CardPerformance;
import com.woorifisa.won_card_core_server.domain.performance.repository.CardPerformanceRepository;
import com.woorifisa.won_card_core_server.global.exception.code.CommonErrorCode;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardGetService {

    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String REWARD_STATUS_SATISFIED = "기준 충족";
    private static final String REWARD_STATUS_NOT_SATISFIED = "기준 미달";

    private final CardUserRepository cardUserRepository;
    private final CardPerformanceRepository cardPerformanceRepository;

    public CurrentRewardsAmount getCurrentReward(UUID userUuid) {
        validateUserUuid(userUuid);
        String baseMonth = YearMonth.now(SEOUL_ZONE_ID).toString();

        cardUserRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.CARD_USER_NOT_FOUND));

        CardPerformance performance = cardPerformanceRepository.findByUserUuidAndBaseMonth(userUuid, baseMonth)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.REWARD_LEDGER_NOT_FOUND));

        return CurrentRewardsAmount.from(performance, getRewardStatus(performance));
    }

    private void validateUserUuid(UUID userUuid) {
        if (userUuid == null) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        }
    }

    private String getRewardStatus(CardPerformance performance) {
        BigDecimal previousMonthSpendAmount = performance.getPreviousMonthSpendAmount();
        if (previousMonthSpendAmount == null || previousMonthSpendAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(RewardErrorCode.INVALID_PERFORMANCE_AMOUNT);
        }

        if (BigDecimal.ZERO.compareTo(previousMonthSpendAmount) == 0) {
            return REWARD_STATUS_NOT_SATISFIED;
        }

        return REWARD_STATUS_SATISFIED;
    }
}
