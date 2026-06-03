package com.woorifisa.won_card_core_server.domain.reward.api;

import com.woorifisa.won_card_core_server.domain.reward.dto.request.SweepRequestOutboxFailRequest;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.SweepRequestOutboxResponse;
import com.woorifisa.won_card_core_server.domain.reward.service.SweepRequestOutboxService;
import com.woorifisa.won_card_core_server.global.response.ApiResponse;
import com.woorifisa.won_card_core_server.global.response.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/cards/rewards/sweep-request-outboxes")
public class InternalSweepRequestOutboxApi {

    private final SweepRequestOutboxService outboxService;

    @PostMapping("/claim")
    public ResponseEntity<ApiResponse<List<SweepRequestOutboxResponse>>> claim(@RequestParam int size) {
        List<SweepRequestOutboxResponse> response = outboxService.claimReady(size);

        return ResponseEntity
                .status(SuccessStatus.REWARD_SWEEP_OUTBOX_CLAIMED.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REWARD_SWEEP_OUTBOX_CLAIMED, response));
    }

    @PostMapping("/{outboxId}/published")
    public ResponseEntity<ApiResponse<Void>> markPublished(@PathVariable Long outboxId) {
        outboxService.markPublished(outboxId);

        return ResponseEntity
                .status(SuccessStatus.REWARD_SWEEP_OUTBOX_PUBLISHED.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REWARD_SWEEP_OUTBOX_PUBLISHED));
    }

    @PostMapping("/{outboxId}/failed")
    public ResponseEntity<ApiResponse<Void>> markFailed(
            @PathVariable Long outboxId,
            @RequestBody SweepRequestOutboxFailRequest request
    ) {
        outboxService.markFailed(outboxId, request.errorMessage());

        return ResponseEntity
                .status(SuccessStatus.REWARD_SWEEP_OUTBOX_FAILED.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REWARD_SWEEP_OUTBOX_FAILED));
    }
}
