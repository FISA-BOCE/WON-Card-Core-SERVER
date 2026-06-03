package com.woorifisa.won_card_core_server.domain.reward.service;

import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepBatchStartResponse;
import com.woorifisa.won_card_core_server.domain.reward.dto.result.RewardSweepChunkReservationResult;
import com.woorifisa.won_card_core_server.domain.reward.model.RewardSweepBatchExecution;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.RewardSweepBatchStatus;
import com.woorifisa.won_card_core_server.domain.reward.repository.RewardSweepBatchExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardSweepBatchService {

    private static final int DEFAULT_CHUNK_SIZE = 300;

    private final RewardSweepBatchExecutionRepository batchRepository;
    private final RewardSweepBatchChunkReservationService reservationService;

    public RewardSweepBatchStartResponse start(String baseMonth, Integer chunkSize) {
        validateBaseMonth(baseMonth);
        validateNoRunningBatch(baseMonth);

        RewardSweepBatchExecution batch = batchRepository.save(RewardSweepBatchExecution.start(baseMonth, LocalDateTime.now()));

        int size = chunkSize == null ? DEFAULT_CHUNK_SIZE : chunkSize;
        Long lastSeenId = 0L;

        while (true) {
            RewardSweepChunkReservationResult result = reservationService.reserve(batch.getBatchExecutionId(), baseMonth, lastSeenId, size);

            if (result.reservedCount() == 0) {
                break;
            }

            lastSeenId = result.lastProcessedPointLedgerId();
        }

        RewardSweepBatchExecution updatedBatch = batchRepository.findById(batch.getBatchExecutionId())
                .orElseThrow();

        return RewardSweepBatchStartResponse.from(updatedBatch);
    }

    private void validateNoRunningBatch(String baseMonth) {
        boolean exists = batchRepository.existsByBaseMonthAndStatusIn(
                baseMonth,
                List.of(RewardSweepBatchStatus.RUNNING, RewardSweepBatchStatus.PUBLISHED)
        );

        if (exists) {
            throw new IllegalStateException("이미 실행 중인 스윕 배치가 있습니다.");
        }
    }

    private void validateBaseMonth(String baseMonth) {
        if (baseMonth == null || !baseMonth.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            throw new IllegalArgumentException("잘못된 기준월입니다.");
        }
    }
}
