package com.woorifisa.won_card_core_server.domain.spend.service;

import com.woorifisa.won_card_core_server.domain.card.model.CardUser;
import com.woorifisa.won_card_core_server.domain.card.repository.CardRepository;
import com.woorifisa.won_card_core_server.domain.card.repository.CardUserRepository;
import com.woorifisa.won_card_core_server.domain.performance.model.CardPerformance;
import com.woorifisa.won_card_core_server.domain.performance.repository.CardPerformanceRepository;
import com.woorifisa.won_card_core_server.domain.spend.dto.response.CurrentSpendAmountResponse;
import com.woorifisa.won_card_core_server.domain.spend.exception.code.SpendErrorCode;
import com.woorifisa.won_card_core_server.global.exception.code.CommonErrorCode;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpendSummaryService {

    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");

    private static final Long FIRST_PERFORMANCE_MIN_AMOUNT = 0L;
    private static final Long SECOND_PERFORMANCE_MIN_AMOUNT = 500_000L;
    private static final Long THIRD_PERFORMANCE_MIN_AMOUNT = 1_500_000L;
    private static final Long MIN_REWARD_POINT_LIMIT_AMOUNT = 0L;
    private static final Long MAX_REWARD_POINT_LIMIT_AMOUNT = 40_000L;

    private static final String FIRST_PERFORMANCE_STATUS = "1";
    private static final String SECOND_PERFORMANCE_STATUS = "2";
    private static final String THIRD_PERFORMANCE_STATUS = "3";

    private static final BigDecimal FIRST_REWARD_RATE = new BigDecimal("0.7");
    private static final BigDecimal SECOND_REWARD_RATE = new BigDecimal("1.0");
    private static final BigDecimal THIRD_REWARD_RATE = new BigDecimal("1.2");
    private static final BigDecimal PERCENT_DIVISOR = new BigDecimal("100");

    private final CardUserRepository cardUserRepository;
    private final CardRepository cardRepository;
    private final CardPerformanceRepository cardPerformanceRepository;

    public CurrentSpendAmountResponse getSpendSummary(UUID userUuid) {
        if (userUuid == null) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        }

        CardUser cardUser = cardUserRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new BusinessException(SpendErrorCode.CARD_USER_NOT_FOUND));

        UUID cardUserUuid = cardUser.getCardUserUuid();
        if (!cardRepository.existsByCardUserUuid(cardUserUuid)) {
            throw new BusinessException(SpendErrorCode.CARD_USER_NOT_FOUND);
        }

        String baseMonth = YearMonth.now(SEOUL_ZONE_ID).toString();
        CardPerformance performance = cardPerformanceRepository
                .findByCardUserUuidAndBaseMonth(cardUserUuid, baseMonth)
                .orElseThrow(() -> new BusinessException(SpendErrorCode.CURRENT_SPEND_AMOUNT_NOT_FOUND));

        Long currentSpendAmount = toLong(performance.getCurrentMonthSpendAmount());
        return createSpendAmountResponse(baseMonth, currentSpendAmount);
    }

    private CurrentSpendAmountResponse createSpendAmountResponse(String baseMonth, Long currentSpendAmount) {
        String currentPerformanceStatus = calculatePerformanceStatus(currentSpendAmount);
        BigDecimal currentRewardRate = getRewardRate(currentPerformanceStatus);
        String nextPerformanceStatus = getNextPerformanceStatus(currentPerformanceStatus);
        Long amountRemainingUntilNextPerformance =
                calculateAmountRemainingUntilNextPerformance(currentSpendAmount, currentPerformanceStatus);
        BigDecimal nextRewardRate = getRewardRate(nextPerformanceStatus);
        Long expectedRewardAmount = calculateExpectedRewardAmount(currentSpendAmount, currentRewardRate);

        return CurrentSpendAmountResponse.found(
                baseMonth,
                currentSpendAmount,
                currentRewardRate,
                nextPerformanceStatus,
                amountRemainingUntilNextPerformance,
                nextRewardRate,
                new CurrentSpendAmountResponse.ExpectedReward(
                        currentSpendAmount,
                        currentRewardRate,
                        expectedRewardAmount
                )
        );
    }

    private String calculatePerformanceStatus(Long currentSpendAmount) {
        if (currentSpendAmount < SECOND_PERFORMANCE_MIN_AMOUNT) {
            return FIRST_PERFORMANCE_STATUS;
        }
        if (currentSpendAmount < THIRD_PERFORMANCE_MIN_AMOUNT) {
            return SECOND_PERFORMANCE_STATUS;
        }
        return THIRD_PERFORMANCE_STATUS;
    }

    private String getNextPerformanceStatus(String performanceStatus) {
        if (FIRST_PERFORMANCE_STATUS.equals(performanceStatus)) {
            return SECOND_PERFORMANCE_STATUS;
        }
        return THIRD_PERFORMANCE_STATUS;
    }

    private BigDecimal getRewardRate(String performanceStatus) {
        return switch (performanceStatus) {
            case FIRST_PERFORMANCE_STATUS -> FIRST_REWARD_RATE;
            case SECOND_PERFORMANCE_STATUS -> SECOND_REWARD_RATE;
            case THIRD_PERFORMANCE_STATUS -> THIRD_REWARD_RATE;
            default -> BigDecimal.ZERO;
        };
    }

    private Long calculateAmountRemainingUntilNextPerformance(Long currentSpendAmount, String performanceStatus) {
        Long nextPerformanceMinAmount = switch (performanceStatus) {
            case FIRST_PERFORMANCE_STATUS -> SECOND_PERFORMANCE_MIN_AMOUNT;
            case SECOND_PERFORMANCE_STATUS -> THIRD_PERFORMANCE_MIN_AMOUNT;
            case THIRD_PERFORMANCE_STATUS -> THIRD_PERFORMANCE_MIN_AMOUNT;
            default -> FIRST_PERFORMANCE_MIN_AMOUNT;
        };

        return Math.max(nextPerformanceMinAmount - currentSpendAmount, 0L);
    }

    private Long calculateExpectedRewardAmount(Long currentSpendAmount, BigDecimal rewardRate) {
        Long expectedRewardAmount = BigDecimal.valueOf(currentSpendAmount)
                .multiply(rewardRate)
                .divide(PERCENT_DIVISOR, 0, RoundingMode.DOWN)
                .longValue();

        return Math.max(
                MIN_REWARD_POINT_LIMIT_AMOUNT,
                Math.min(expectedRewardAmount, MAX_REWARD_POINT_LIMIT_AMOUNT)
        );
    }

    private Long toLong(BigDecimal amount) {
        if (amount == null) {
            return 0L;
        }

        return amount.setScale(0, RoundingMode.DOWN).longValue();
    }
}
