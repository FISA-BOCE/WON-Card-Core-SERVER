package com.woorifisa.won_card_core_server.domain.reward.service;

import com.woorifisa.won_card_core_server.domain.performance.exception.code.CardPerformanceErrorCode;
import com.woorifisa.won_card_core_server.domain.performance.model.CardPerformance;
import com.woorifisa.won_card_core_server.domain.performance.repository.CardPerformanceRepository;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepCancelResponse;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepCandidateResponse;
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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardSweepService {

    private final CardPointLedgerRepository cardPointLedgerRepository;
    private final CardPerformanceRepository cardPerformanceRepository;
    private final RewardLedgerValidator rewardLedgerValidator;

    @Transactional
    public RewardSweepRequestResponse requestSweep(UUID cardUserUuid, Long pointLedgerId) {
        rewardLedgerValidator.validateRequired(cardUserUuid, pointLedgerId);

        CardPointLedger pointLedger = cardPointLedgerRepository.findByIdForUpdate(pointLedgerId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.REWARD_LEDGER_NOT_FOUND));

        rewardLedgerValidator.validateOwner(pointLedger, cardUserUuid);
        validateSweepEligible(pointLedger);

        Long pointAmount = pointLedger.getDisplayPointAmount();

        if (pointAmount == null || pointAmount <= 0) {
            throw new BusinessException(RewardErrorCode.REWARD_SWEEP_AMOUNT_INVALID);
        }

        CardPerformance performance = getPerformance(pointLedger);

        pointLedger.markSweepRequested();

        return RewardSweepRequestResponse.from(pointLedger, performance, pointAmount);
    }


    // 해당 월에 해당하는 가능한 스윕 후보들 채널계로 넘겨줄거
    public RewardSweepCandidateResponse getSweepCandidates(String baseMonth) {
        // 월 검증
        validateBaseMonth(baseMonth);

        // 해당 월 && 포인트 획득 상태 (즉, 스윕 가능한 포인트) && 아직 스윕 요청 안 된 조건
        List<CardPointLedger> pointLedgers =
                cardPointLedgerRepository.findSweepCandidates(baseMonth, RewardProcessStatus.EARN, SweepStatus.NONE);

        List<RewardSweepCandidateResponse.RewardSweepCandidateItem> candidates =
                pointLedgers.stream()
                        .map(pointLedger -> {
                            // 원장에서 표시할 포인트 금액 계산
                            Long pointAmount = pointLedger.getDisplayPointAmount();

                            // 레포지토리에서 한 번 검증 후 서비스에서 2차 방어
                            if (pointAmount == null || pointAmount <= 0) {
                                throw new BusinessException(RewardErrorCode.REWARD_SWEEP_AMOUNT_INVALID);
                            }

                            return RewardSweepCandidateResponse.RewardSweepCandidateItem.from(pointLedger, pointAmount);
                        })
                        .toList();

        return new RewardSweepCandidateResponse(baseMonth, candidates);
    }

    @Transactional
    public RewardSweepCancelResponse cancelSweepRequest(UUID cardUserUuid, Long pointLedgerId) {
        rewardLedgerValidator.validateRequired(cardUserUuid, pointLedgerId);

        CardPointLedger pointLedger = cardPointLedgerRepository.findByIdForUpdate(pointLedgerId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.REWARD_LEDGER_NOT_FOUND));

        rewardLedgerValidator.validateOwner(pointLedger, cardUserUuid);

        if (pointLedger.getSweepStatus() == SweepStatus.NONE) {
            return RewardSweepCancelResponse.from(pointLedger);
        }

        if (pointLedger.getSweepStatus() != SweepStatus.REQUESTED) {
            throw new BusinessException(RewardErrorCode.REWARD_SWEEP_CANCEL_NOT_ALLOWED);
        }

        pointLedger.cancelSweepRequested();

        return RewardSweepCancelResponse.from(pointLedger);
    }


    private void validateBaseMonth(String baseMonth) {
        if (baseMonth == null || baseMonth.isBlank() || !baseMonth.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            throw new BusinessException(RewardErrorCode.INVALID_REWARD_BASE_MONTH);
        }
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
