package com.woorifisa.won_card_core_server.domain.reward.service;

import com.woorifisa.won_card_core_server.domain.reward.dto.result.RewardSweepChunkReservationResult;
import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;
import com.woorifisa.won_card_core_server.domain.reward.model.RewardSweepBatchExecution;
import com.woorifisa.won_card_core_server.domain.reward.model.SweepRequestOutbox;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.RewardProcessStatus;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.SweepStatus;
import com.woorifisa.won_card_core_server.domain.reward.repository.CardPointLedgerRepository;
import com.woorifisa.won_card_core_server.domain.reward.repository.RewardSweepBatchExecutionRepository;
import com.woorifisa.won_card_core_server.domain.reward.repository.SweepRequestOutboxRepository;
import com.woorifisa.won_card_core_server.domain.reward.service.factory.SweepRequestPayloadFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardSweepBatchChunkReservationService {

    private final RewardSweepBatchExecutionRepository batchRepository;
    private final CardPointLedgerRepository pointLedgerRepository;
    private final SweepRequestOutboxRepository outboxRepository;
    private final SweepRequestPayloadFactory payloadFactory;

    @Transactional
    public RewardSweepChunkReservationResult reserve(
            Long batchExecutionId,
            String baseMonth,
            Long lastSeenId,
            int chunkSize
    ) {
        RewardSweepBatchExecution batch = batchRepository.findById(batchExecutionId)
                .orElseThrow();

        List<CardPointLedger> candidates = pointLedgerRepository.findSweepCandidateChunk(
                baseMonth,
                RewardProcessStatus.EARN,
                SweepStatus.NONE,
                lastSeenId,
                PageRequest.of(0, chunkSize)
        );

        if (candidates.isEmpty()) {
            return new RewardSweepChunkReservationResult(0, lastSeenId);
        }

        LocalDateTime requestedAt = LocalDateTime.now();

        for (CardPointLedger ledger : candidates) {
            Long pointAmount = ledger.getDisplayPointAmount();
            String idempotencyKey = "CARD_SWEEP:%d:%s".formatted(
                    ledger.getPointLedgerId(),
                    ledger.getBaseMonth()
            );

            ledger.markSweepRequested(batchExecutionId, idempotencyKey, requestedAt);

            String payload = payloadFactory.create(ledger, pointAmount);
            outboxRepository.save(SweepRequestOutbox.ready(batchExecutionId, ledger, payload));
        }

        Long lastProcessedId = candidates.get(candidates.size() - 1).getPointLedgerId();
        batch.addRequested(candidates.size(), lastProcessedId);

        return new RewardSweepChunkReservationResult(candidates.size(), lastProcessedId);
    }
}
