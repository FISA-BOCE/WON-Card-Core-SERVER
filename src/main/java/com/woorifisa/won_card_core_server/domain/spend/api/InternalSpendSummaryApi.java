package com.woorifisa.won_card_core_server.domain.spend.api;

import com.woorifisa.won_card_core_server.domain.spend.dto.response.CurrentSpendAmountResponse;
import com.woorifisa.won_card_core_server.domain.spend.service.SpendSummaryService;
import com.woorifisa.won_card_core_server.global.response.ApiResponse;
import com.woorifisa.won_card_core_server.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Spend", description = "카드 이용 금액 요약 API")
public class InternalSpendSummaryApi {

    private final SpendSummaryService spendSummaryService;

    @Operation(summary = "당월 이용 금액 조회", description = "카드 사용자의 당월 이용 금액과 예상 리워드를 조회하는 API입니다.")
    @GetMapping("/internal/cards/spend-summary")
    public ResponseEntity<ApiResponse<CurrentSpendAmountResponse>> getSpendSummary(
            @RequestHeader("X-User-UUID") UUID userUuid
    ) {
        CurrentSpendAmountResponse response = spendSummaryService.getSpendSummary(userUuid);

        return ResponseEntity
                .status(SuccessStatus.CURRENT_SPEND_AMOUNT_FOUND.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CURRENT_SPEND_AMOUNT_FOUND, response));
    }
}
