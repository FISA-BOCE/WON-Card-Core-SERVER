package com.woorifisa.won_card_core_server.domain.admin.service;

import com.woorifisa.won_card_core_server.domain.admin.dto.response.AdminSweepRequestItemResponse;
import com.woorifisa.won_card_core_server.domain.admin.dto.response.AdminSweepRequestListResponse;
import com.woorifisa.won_card_core_server.domain.admin.dto.response.AdminSweepRequestSummaryResponse;
import com.woorifisa.won_card_core_server.domain.reward.exception.code.RewardErrorCode;
import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.SweepStatus;
import com.woorifisa.won_card_core_server.domain.reward.repository.CardPointLedgerRepository;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminRewardSweepQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final CardPointLedgerRepository cardPointLedgerRepository;

    public AdminSweepRequestListResponse getSweepRequests(
            SweepStatus status,
            String baseMonth,
            UUID cardUserUuid,
            Long sweepRequestId,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(normalizePage(page), normalizeSize(size));
        Page<CardPointLedger> sweepRequests = cardPointLedgerRepository.findAdminSweepRequests(
                status,
                normalizeBlank(baseMonth),
                cardUserUuid,
                sweepRequestId,
                pageable
        );

        List<AdminSweepRequestItemResponse> items = sweepRequests.getContent()
                .stream()
                .map(AdminSweepRequestItemResponse::from)
                .toList();

        return new AdminSweepRequestListResponse(
                getSummary(normalizeBlank(baseMonth), cardUserUuid, sweepRequestId),
                items,
                sweepRequests.getNumber(),
                sweepRequests.getSize(),
                sweepRequests.getTotalElements(),
                sweepRequests.getTotalPages()
        );
    }

    public AdminSweepRequestItemResponse getSweepRequest(Long sweepRequestId) {
        CardPointLedger ledger = cardPointLedgerRepository.findBySweepRequestId(sweepRequestId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.REWARD_LEDGER_NOT_FOUND));

        return AdminSweepRequestItemResponse.from(ledger);
    }

    public AdminSweepRequestSummaryResponse getSummary(
            String baseMonth,
            UUID cardUserUuid,
            Long sweepRequestId
    ) {
        String normalizedBaseMonth = normalizeBlank(baseMonth);

        long createdCount = cardPointLedgerRepository.countAdminSweepRequests(
                SweepStatus.NONE,
                normalizedBaseMonth,
                cardUserUuid,
                sweepRequestId
        );
        long processingCount = cardPointLedgerRepository.countAdminSweepRequests(
                SweepStatus.REQUESTED,
                normalizedBaseMonth,
                cardUserUuid,
                sweepRequestId
        );
        long completedCount = cardPointLedgerRepository.countAdminSweepRequests(
                SweepStatus.COMPLETED,
                normalizedBaseMonth,
                cardUserUuid,
                sweepRequestId
        );
        long failedCount = cardPointLedgerRepository.countAdminSweepRequests(
                SweepStatus.FAILED,
                normalizedBaseMonth,
                cardUserUuid,
                sweepRequestId
        );

        return new AdminSweepRequestSummaryResponse(
                createdCount + processingCount + completedCount + failedCount,
                createdCount,
                processingCount,
                completedCount,
                failedCount
        );
    }

    private int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return 20;
        }

        return Math.min(size, MAX_PAGE_SIZE);
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value;
    }
}
