package com.woorifisa.won_card_core_server.domain.reward.service;

import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepBatchStartResponse;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepReservationResponse;
import com.woorifisa.won_card_core_server.domain.reward.dto.result.RewardSweepChunkReservationResult;
import com.woorifisa.won_card_core_server.domain.reward.exception.code.RewardErrorCode;
import com.woorifisa.won_card_core_server.domain.reward.model.RewardSweepBatchExecution;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.RewardSweepBatchStatus;
import com.woorifisa.won_card_core_server.domain.reward.repository.RewardSweepBatchExecutionRepository;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardSweepBatchService {

    private static final int DEFAULT_CHUNK_SIZE = 300;

    private final RewardSweepBatchExecutionRepository batchRepository;
    private final RewardSweepBatchChunkReservationService reservationService;

    @Transactional
    public RewardSweepBatchStartResponse start(String baseMonth) {
        validateBaseMonth(baseMonth);
        validateNoRunningBatch(baseMonth);

        RewardSweepBatchExecution batch = batchRepository.save(RewardSweepBatchExecution.start(baseMonth, LocalDateTime.now()));

        return RewardSweepBatchStartResponse.from(batch);
    }

    public RewardSweepReservationResponse reserve(Long batchExecutionId, Integer size) {
        int chunkSize = size == null ? DEFAULT_CHUNK_SIZE : size;
        validateChunkSize(chunkSize);

        RewardSweepChunkReservationResult result = reservationService.reserve(batchExecutionId, chunkSize);

        RewardSweepBatchExecution batch = batchRepository.findById(batchExecutionId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.REWARD_SWEEP_BATCH_NOT_FOUND));

        return RewardSweepReservationResponse.from(batch, result);
    }

    private void validateNoRunningBatch(String baseMonth) {
        boolean exists = batchRepository.existsByBaseMonthAndStatusIn(
                baseMonth,
                List.of(RewardSweepBatchStatus.RUNNING)
        );

        if (exists) {
            throw new BusinessException(RewardErrorCode.REWARD_SWEEP_BATCH_ALREADY_RUNNING);
        }
    }

    private void validateBaseMonth(String baseMonth) {
        if (baseMonth == null || !baseMonth.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            throw new BusinessException(RewardErrorCode.INVALID_REWARD_BASE_MONTH);
        }
    }

    private void validateChunkSize(int chunkSize) {
        if (chunkSize <= 0) {
            throw new BusinessException(RewardErrorCode.INVALID_REWARD_SWEEP_BATCH_SIZE);
        }
    }
}
