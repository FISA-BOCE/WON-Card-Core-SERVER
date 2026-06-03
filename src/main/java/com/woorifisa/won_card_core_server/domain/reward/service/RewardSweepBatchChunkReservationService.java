package com.woorifisa.won_card_core_server.domain.reward.service;

import com.woorifisa.won_card_core_server.domain.reward.dto.result.RewardSweepChunkReservationResult;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepReservedItemResponse;
import com.woorifisa.won_card_core_server.domain.reward.exception.code.RewardErrorCode;
import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;
import com.woorifisa.won_card_core_server.domain.reward.model.RewardSweepBatchExecution;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.RewardProcessStatus;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.SweepStatus;
import com.woorifisa.won_card_core_server.domain.reward.repository.CardPointLedgerRepository;
import com.woorifisa.won_card_core_server.domain.reward.repository.RewardSweepBatchExecutionRepository;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardSweepBatchChunkReservationService {

    private final RewardSweepBatchExecutionRepository batchRepository;
    private final CardPointLedgerRepository pointLedgerRepository;

    @Transactional
    public RewardSweepChunkReservationResult reserve(
            Long batchExecutionId,
            int chunkSize
    ) {
        RewardSweepBatchExecution batch = batchRepository.findById(batchExecutionId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.REWARD_SWEEP_BATCH_NOT_FOUND));

        Long lastSeenId = batch.getLastProcessedPointLedgerId() == null
                ? 0L
                : batch.getLastProcessedPointLedgerId();

        List<CardPointLedger> candidates = pointLedgerRepository.findSweepCandidateChunk(
                batch.getBaseMonth(),
                RewardProcessStatus.EARN,
                SweepStatus.NONE,
                lastSeenId,
                PageRequest.of(0, chunkSize)
        );

        if (candidates.isEmpty()) {
            batch.completeWhenNoCandidates();
            return new RewardSweepChunkReservationResult(0, lastSeenId, List.of());
        }

        LocalDateTime requestedAt = LocalDateTime.now();
        List<RewardSweepReservedItemResponse> reservedItems = new ArrayList<>();

        for (CardPointLedger ledger : candidates) {
            Long pointAmount = ledger.getDisplayPointAmount();
            String idempotencyKey = "CARD_SWEEP:%d:%s".formatted(
                    ledger.getPointLedgerId(),
                    ledger.getBaseMonth()
            );

            ledger.markSweepRequested(batchExecutionId, idempotencyKey, requestedAt);

            reservedItems.add(RewardSweepReservedItemResponse.from(ledger, pointAmount));
        }

        Long lastProcessedId = candidates.get(candidates.size() - 1).getPointLedgerId();
        batch.addRequested(candidates.size(), lastProcessedId);

        return new RewardSweepChunkReservationResult(candidates.size(), lastProcessedId, reservedItems);
    }
}
