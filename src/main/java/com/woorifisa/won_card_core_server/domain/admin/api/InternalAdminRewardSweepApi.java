package com.woorifisa.won_card_core_server.domain.admin.api;

import com.woorifisa.won_card_core_server.domain.admin.dto.response.AdminSweepRequestItemResponse;
import com.woorifisa.won_card_core_server.domain.admin.dto.response.AdminSweepRequestListResponse;
import com.woorifisa.won_card_core_server.domain.admin.dto.response.AdminSweepRequestSummaryResponse;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.SweepStatus;
import com.woorifisa.won_card_core_server.domain.admin.service.AdminRewardSweepQueryService;
import com.woorifisa.won_card_core_server.global.response.ApiResponse;
import com.woorifisa.won_card_core_server.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/admin/cards/sweep-requests")
@Tag(name = "Admin Sweep Internal API", description = "관리자 화면에서 카드 리워드 스윕 요청 원장 상태를 조회하기 위한 내부 API")
public class InternalAdminRewardSweepApi {

    private final AdminRewardSweepQueryService adminRewardSweepQueryService;

    @Operation(
            summary = "관리자 스윕 요청 목록 조회",
            description = "card_point_ledger 기준으로 스윕 요청 목록과 상태별 요약 정보를 조회합니다. 상태, 기준월, 카드 사용자 UUID, 스윕 요청 ID로 필터링할 수 있으며 페이지네이션을 지원합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<AdminSweepRequestListResponse>> getSweepRequests(
            @RequestParam(required = false) SweepStatus status,
            @RequestParam(required = false) String baseMonth,
            @RequestParam(required = false) UUID cardUserUuid,
            @RequestParam(required = false) Long sweepRequestId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        AdminSweepRequestListResponse response = adminRewardSweepQueryService.getSweepRequests(
                status,
                baseMonth,
                cardUserUuid,
                sweepRequestId,
                page,
                size
        );

        return ResponseEntity
                .status(SuccessStatus.ADMIN_SWEEP_REQUESTS_FOUND.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.ADMIN_SWEEP_REQUESTS_FOUND, response));
    }

    @Operation(
            summary = "관리자 스윕 요청 요약 조회",
            description = "card_point_ledger 기준으로 스윕 요청의 전체, 생성, 처리중, 완료, 실패 건수를 조회합니다. 기준월, 카드 사용자 UUID, 스윕 요청 ID 조건을 적용할 수 있습니다."
    )
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AdminSweepRequestSummaryResponse>> getSummary(
            @RequestParam(required = false) String baseMonth,
            @RequestParam(required = false) UUID cardUserUuid,
            @RequestParam(required = false) Long sweepRequestId
    ) {
        AdminSweepRequestSummaryResponse response = adminRewardSweepQueryService.getSummary(
                baseMonth,
                cardUserUuid,
                sweepRequestId
        );

        return ResponseEntity
                .status(SuccessStatus.ADMIN_SWEEP_REQUEST_SUMMARY_FOUND.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.ADMIN_SWEEP_REQUEST_SUMMARY_FOUND, response));
    }

    @Operation(
            summary = "관리자 스윕 요청 상세 조회",
            description = "스윕 요청 ID로 카드 리워드 원장의 스윕 상태, 실패 코드, 실패 메시지, 요청/완료 시각을 조회합니다."
    )
    @GetMapping("/{sweepRequestId}")
    public ResponseEntity<ApiResponse<AdminSweepRequestItemResponse>> getSweepRequest(
            @PathVariable Long sweepRequestId
    ) {
        AdminSweepRequestItemResponse response = adminRewardSweepQueryService.getSweepRequest(sweepRequestId);

        return ResponseEntity
                .status(SuccessStatus.ADMIN_SWEEP_REQUEST_DETAIL_FOUND.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.ADMIN_SWEEP_REQUEST_DETAIL_FOUND, response));
    }
}
