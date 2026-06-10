package com.woorifisa.won_card_core_server.domain.admin.api;

import com.woorifisa.won_card_core_server.domain.admin.dto.response.AdminSweepRequestItemResponse;
import com.woorifisa.won_card_core_server.domain.admin.dto.response.AdminSweepRequestListResponse;
import com.woorifisa.won_card_core_server.domain.admin.dto.response.AdminSweepRequestSummaryResponse;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.SweepStatus;
import com.woorifisa.won_card_core_server.domain.admin.service.AdminRewardSweepQueryService;
import com.woorifisa.won_card_core_server.global.response.ApiResponse;
import com.woorifisa.won_card_core_server.global.response.SuccessStatus;
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
public class InternalAdminRewardSweepApi {

    private final AdminRewardSweepQueryService adminRewardSweepQueryService;

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
