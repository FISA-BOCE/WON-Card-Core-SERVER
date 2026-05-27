package com.woorifisa.won_card_core_server.domain.reward.api;

import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardLedgerDetailResponse;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardLedgerResponse;
import com.woorifisa.won_card_core_server.domain.reward.service.RewardLedgerService;
import com.woorifisa.won_card_core_server.global.response.ApiResponse;
import com.woorifisa.won_card_core_server.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reward Internal API", description = "자동 투자 리워드 관련 API")
public class InternalRewardLedgerApi {

    private final RewardLedgerService rewardLedgerService;

    @Operation(summary = "자동 투자 리워드 목록 조회", description = "자동 투자 된 리워드 목록 조회 페이지에서 사용되는 API입니다.")
    @GetMapping("/internal/cards/rewards/ledger")
    public ResponseEntity<ApiResponse<RewardLedgerResponse>> getRewardLedger(
            @RequestHeader("X-Card-User-UUID") UUID cardUserUuid,
            @RequestParam(required = false) String type
    ) {
        RewardLedgerResponse response = rewardLedgerService.getRewardLedger(cardUserUuid, type);

        return ResponseEntity
                .status(SuccessStatus.REWARD_LEDGER_FOUND.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REWARD_LEDGER_FOUND, response));
    }

    @Operation(summary = "자동 투자 리워드 상세 목록 조회", description = "자동 투자 된 리워드 상세 조회 API입니다.")
    @GetMapping("/internal/cards/rewards/ledger/{pointLedgerId}")
    public ResponseEntity<ApiResponse<RewardLedgerDetailResponse>> getRewardLedgerDetail(
            @RequestHeader("X-Card-User-UUID") UUID cardUserUuid,
            @PathVariable Long pointLedgerId
    ) {
        RewardLedgerDetailResponse response = rewardLedgerService.getRewardLedgerDetail(cardUserUuid, pointLedgerId);

        return ResponseEntity
                .status(SuccessStatus.REWARD_LEDGER_DETAIL_FOUND.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REWARD_LEDGER_DETAIL_FOUND, response));
    }

}
