package com.woorifisa.won_card_core_server.domain.reward.service;

import com.woorifisa.won_card_core_server.domain.performance.exception.code.CardPerformanceErrorCode;
import com.woorifisa.won_card_core_server.domain.performance.model.CardPerformance;
import com.woorifisa.won_card_core_server.domain.performance.repository.CardPerformanceRepository;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepRequestResponse;
import com.woorifisa.won_card_core_server.domain.reward.exception.code.RewardErrorCode;
import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.RewardProcessStatus;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.SweepStatus;
import com.woorifisa.won_card_core_server.domain.reward.repository.CardPointLedgerRepository;
import com.woorifisa.won_card_core_server.domain.reward.service.validator.RewardLedgerValidator;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RewardSweepService {

    private final CardPointLedgerRepository cardPointLedgerRepository;
    private final CardPerformanceRepository cardPerformanceRepository;
    private final RewardLedgerValidator rewardLedgerValidator;

    @Transactional
    public RewardSweepRequestResponse requestSweep(
            UUID cardUserUuid,
            Long pointLedgerId
    ) {
        rewardLedgerValidator.validateRequired(cardUserUuid, pointLedgerId);

        CardPointLedger pointLedger = cardPointLedgerRepository.findByIdForUpdate(pointLedgerId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.REWARD_LEDGER_NOT_FOUND));

        rewardLedgerValidator.validateOwner(pointLedger, cardUserUuid);
        validateSweepEligible(pointLedger);

        Long pointAmount = pointLedger.getDisplayPointAmount();

        if (pointAmount <= 0) {
            throw new BusinessException(RewardErrorCode.REWARD_SWEEP_AMOUNT_INVALID);
        }

        CardPerformance performance = getPerformance(pointLedger);

        pointLedger.markSweepRequested();

        return RewardSweepRequestResponse.from(pointLedger, performance, pointAmount);
    }

    private CardPerformance getPerformance(CardPointLedger pointLedger) {
        Long performanceId = pointLedger.getPerformanceId();

        if (performanceId == null) {
            throw new BusinessException(CardPerformanceErrorCode.PERFORMANCE_NOT_FOUND);
        }

        return cardPerformanceRepository
                .findByPerformanceIdAndCardUserUuid(performanceId, pointLedger.getCardUserUuid())
                .orElseThrow(() -> new BusinessException(CardPerformanceErrorCode.PERFORMANCE_NOT_FOUND));
    }

    private void validateSweepEligible(CardPointLedger pointLedger) {
        // 리워드가 실제로 적립된 원장만 스윕 가능
        if (pointLedger.getRewardProcessStatus() != RewardProcessStatus.EARN) {
            throw new BusinessException(RewardErrorCode.REWARD_SWEEP_NOT_ELIGIBLE);
        }

        // 아직 스윕 요청된 적 없는 원장만 스윕 가능
        if (pointLedger.getSweepStatus() != SweepStatus.NONE) {
            throw new BusinessException(RewardErrorCode.REWARD_SWEEP_ALREADY_REQUESTED);
        }
    }
}
