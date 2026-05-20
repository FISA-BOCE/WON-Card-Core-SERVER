package com.woorifisa.won_card_core_server.domain.reward.api;

import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardLedgerResponse;
import com.woorifisa.won_card_core_server.domain.reward.service.RewardLedgerService;
import com.woorifisa.won_card_core_server.global.response.ApiResponse;
import com.woorifisa.won_card_core_server.global.response.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InternalRewardLedgerApi {

    private final RewardLedgerService rewardLedgerService;

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

}
