package com.woorifisa.won_card_core_server.domain.performance.api;

import com.woorifisa.won_card_core_server.domain.performance.dto.response.PreviousPerformanceResponse;
import com.woorifisa.won_card_core_server.domain.performance.service.CardPerformanceService;
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
@Tag(name = "Performance", description = "카드 실적 조회 API")
public class InternalCardPerformanceApi {

    private final CardPerformanceService cardPerformanceService;

    @Operation(summary = "전월 실적 조회", description = "사용자의 전월 실적과 리워드 지급 금액을 조회하는 API입니다.")
    @GetMapping("/internal/cards/performance/monthly")
    public ResponseEntity<ApiResponse<PreviousPerformanceResponse>> getPreviousPerformance(
            @RequestHeader("X-User-UUID") UUID userUuid
    ) {
        PreviousPerformanceResponse response = cardPerformanceService.getPreviousPerformance(userUuid);

        return ResponseEntity
                .status(SuccessStatus.PREVIOUS_PERFORMANCE_FOUND.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.PREVIOUS_PERFORMANCE_FOUND, response));
    }
}
