package com.woorifisa.won_card_core_server.domain.reward.service;

import com.woorifisa.won_card_core_server.domain.performance.exception.code.CardPerformanceErrorCode;
import com.woorifisa.won_card_core_server.domain.performance.model.CardPerformance;
import com.woorifisa.won_card_core_server.domain.performance.repository.CardPerformanceRepository;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardLedgerDetailResponse;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardLedgerResponse;
import com.woorifisa.won_card_core_server.domain.reward.exception.code.RewardErrorCode;
import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.RewardProcessStatus;
import com.woorifisa.won_card_core_server.domain.reward.repository.CardPointLedgerRepository;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardLedgerService {

    private static final Long REWARD_TARGET_AMOUNT = 500_000L;

    private final CardPointLedgerRepository cardPointLedgerRepository;
    private final CardPerformanceRepository cardPerformanceRepository;

    public RewardLedgerResponse getRewardLedger(UUID cardUserUuid, String type) {

        if (cardUserUuid == null) {
            throw new BusinessException(RewardErrorCode.REWARD_LEDGER_NOT_FOUND);
        }

        RewardProcessStatus rewardProcessStatus = RewardProcessStatus.from(type);

        int baseYear = Year.now().getValue();
        LocalDateTime startDateTime = LocalDateTime.of(baseYear, 1, 1, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(baseYear + 1, 1, 1, 0, 0);

        Long totalAccumulatedAmount = getTotalAccumulatedAmount(cardUserUuid, startDateTime, endDateTime);

        List<CardPointLedger> ledgers = getLedgers(cardUserUuid, rewardProcessStatus, startDateTime, endDateTime);

        return new RewardLedgerResponse(baseYear, totalAccumulatedAmount,
                ledgers.stream()
                        .map(RewardLedgerResponse.RewardLedgerItem::from)
                        .toList()
        );
    }

    public RewardLedgerDetailResponse getRewardLedgerDetail(UUID cardUserUuid, Long pointLedgerId) {

        // ID 값 Null 체크
        if (cardUserUuid == null || pointLedgerId == null) {
            throw new BusinessException(RewardErrorCode.REWARD_LEDGER_NOT_FOUND);
        }

        // pointLedger ID 값 체크
        CardPointLedger pointLedger = cardPointLedgerRepository.findById(pointLedgerId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.REWARD_LEDGER_NOT_FOUND));

        // 본인 소유 리워드인지 확인
        validateOwner(pointLedger, cardUserUuid);

        // 올바른 상태값인지 확인
        validateRewardProcessStatus(pointLedger);

        CardPerformance performance = getPerformance(pointLedger);
        Object detail = createRewardDetail(pointLedger, performance);

        Long pointAmount = getDetailPointAmount(pointLedger);

        return RewardLedgerDetailResponse.from(pointLedger, pointAmount, detail);
    }

    private Long getDetailPointAmount(CardPointLedger pointLedger) {
        if (pointLedger.getRewardProcessStatus() == RewardProcessStatus.NOT_APPLIED) {
            return 0L;
        }

        return pointLedger.getDisplayPointAmount();
    }

    private Object createRewardDetail(
            CardPointLedger pointLedger,
            CardPerformance performance
    ) {
        return switch (pointLedger.getRewardProcessStatus()) {
            case EARN -> createEarnDetail(performance);
            case NOT_APPLIED -> createNotAppliedDetail(performance);
            case ALL -> throw new BusinessException(RewardErrorCode.INVALID_REWARD_LEDGER_STATUS);
        };
    }

    private RewardLedgerDetailResponse.EarnRewardDetail createEarnDetail(
            CardPerformance performance
    ) {
        return new RewardLedgerDetailResponse.EarnRewardDetail(
                toLong(performance.getPreviousMonthSpendAmount()),
                REWARD_TARGET_AMOUNT
        );
    }

    private RewardLedgerDetailResponse.NotAppliedRewardDetail createNotAppliedDetail(
            CardPerformance performance
    ) {
        Long previousMonthSpendAmount = toLong(performance.getPreviousMonthSpendAmount());
        Long shortfallAmount = Math.max(REWARD_TARGET_AMOUNT - previousMonthSpendAmount, 0L);

        return new RewardLedgerDetailResponse.NotAppliedRewardDetail(
                previousMonthSpendAmount,
                REWARD_TARGET_AMOUNT,
                shortfallAmount
        );
    }

    private CardPerformance getPerformance(CardPointLedger pointLedger) {
        Long performanceId = pointLedger.getPerformanceId();

        if (performanceId == null) {
            throw new BusinessException(CardPerformanceErrorCode.PERFORMANCE_NOT_FOUND);
        }

        return cardPerformanceRepository.findByPerformanceId(performanceId)
                .orElseThrow(() -> new BusinessException(CardPerformanceErrorCode.PERFORMANCE_NOT_FOUND));
    }

    private void validateRewardProcessStatus(CardPointLedger pointLedger) {
        if (pointLedger.getRewardProcessStatus() == null
                || pointLedger.getRewardProcessStatus().isAll()) {
            throw new BusinessException(RewardErrorCode.INVALID_REWARD_LEDGER_STATUS);
        }
    }

    private void validateOwner(CardPointLedger pointLedger, UUID cardUserUuid) {
        if (!Objects.equals(pointLedger.getCardUserUuid(), cardUserUuid)) {
            throw new BusinessException(RewardErrorCode.REWARD_LEDGER_FORBIDDEN);
        }
    }

    private Long getTotalAccumulatedAmount(UUID cardUserUuid, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        BigDecimal totalAmount = cardPointLedgerRepository.sumEarnAmount(cardUserUuid, startDateTime, endDateTime);

        return totalAmount.setScale(0, RoundingMode.DOWN).longValue();
    }

    private List<CardPointLedger> getLedgers(UUID cardUserUuid, RewardProcessStatus rewardProcessStatus,
                                             LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (rewardProcessStatus.isAll()) {
            return cardPointLedgerRepository.findRewardLedgers(cardUserUuid, startDateTime, endDateTime);
        }

        return cardPointLedgerRepository.findRewardLedgersByStatus(cardUserUuid, rewardProcessStatus, startDateTime, endDateTime);
    }

    private Long toLong(BigDecimal amount) {
        if (amount == null) {
            return 0L;
        }

        return amount.setScale(0, RoundingMode.DOWN).longValue();
    }
}
