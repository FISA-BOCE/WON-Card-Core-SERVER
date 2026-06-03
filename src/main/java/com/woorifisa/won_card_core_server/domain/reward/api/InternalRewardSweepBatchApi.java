package com.woorifisa.won_card_core_server.domain.reward.api;

import com.woorifisa.won_card_core_server.domain.reward.dto.request.RewardSweepBatchStartRequest;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepBatchStartResponse;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepReservationResponse;
import com.woorifisa.won_card_core_server.domain.reward.service.RewardSweepBatchService;
import com.woorifisa.won_card_core_server.global.response.ApiResponse;
import com.woorifisa.won_card_core_server.global.response.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/cards/rewards/sweep-batches")
public class InternalRewardSweepBatchApi {

    private final RewardSweepBatchService rewardSweepBatchService;

    @PostMapping
    public ResponseEntity<ApiResponse<RewardSweepBatchStartResponse>> start(
            @RequestBody RewardSweepBatchStartRequest request
    ) {
        RewardSweepBatchStartResponse response = rewardSweepBatchService.start(request.baseMonth(), request.chunkSize());

        return ResponseEntity
                .status(SuccessStatus.REWARD_SWEEP_BATCH_STARTED.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REWARD_SWEEP_BATCH_STARTED, response));
    }

    @PostMapping("/{batchExecutionId}/reservations")
    public ResponseEntity<ApiResponse<RewardSweepReservationResponse>> reserve(
            @PathVariable Long batchExecutionId,
            @RequestParam(required = false) Integer size
    ) {
        RewardSweepReservationResponse response = rewardSweepBatchService.reserve(batchExecutionId, size);

        return ResponseEntity
                .status(SuccessStatus.REWARD_SWEEP_BATCH_RESERVED.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REWARD_SWEEP_BATCH_RESERVED, response));
    }
}
