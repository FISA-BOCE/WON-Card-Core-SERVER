package com.woorifisa.won_card_core_server.domain.reward.api;

import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepCandidateResponse;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepRequestResponse;
import com.woorifisa.won_card_core_server.domain.reward.service.RewardSweepService;
import com.woorifisa.won_card_core_server.global.response.ApiResponse;
import com.woorifisa.won_card_core_server.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Reward Sweep Internal API", description = "리워드 포인트 스윕 상태를 관리하는 내부 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/cards/rewards")
public class InternalRewardSweepApi {

    private final RewardSweepService rewardSweepService;

    @Operation(
            summary = "리워드 원장 스윕 요청 선점",
            description = "카드 사용자 UUID와 포인트 원장 ID를 기준으로 스윕 가능 여부를 검증하고, 원장을 REQUESTED 상태로 선점합니다."
    )
    @PostMapping("/ledger/{pointLedgerId}/sweep-request")
    public ResponseEntity<ApiResponse<RewardSweepRequestResponse>> requestSweep(
            @RequestHeader("X-Card-User-UUID") UUID cardUserUuid,
            @PathVariable Long pointLedgerId
    ) {
        RewardSweepRequestResponse response =
                rewardSweepService.requestSweep(cardUserUuid, pointLedgerId);

        return ResponseEntity
                .status(SuccessStatus.REWARD_SWEEP_REQUESTED.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REWARD_SWEEP_REQUESTED, response));
    }

    @Operation(
            summary = "자동 스윕 후보 리워드 원장 조회",
            description = "기준월의 적립 완료 리워드 중 아직 스윕 요청되지 않은 원장 목록을 조회합니다."
    )
    @GetMapping("/ledger/sweep-candidates")
    public ResponseEntity<ApiResponse<RewardSweepCandidateResponse>> getSweepCandidates(
            @RequestParam String baseMonth
    ) {
        RewardSweepCandidateResponse response =
                rewardSweepService.getSweepCandidates(baseMonth);

        return ResponseEntity
                .status(SuccessStatus.REWARD_SWEEP_CANDIDATES_FOUND.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REWARD_SWEEP_CANDIDATES_FOUND, response));
    }

}
