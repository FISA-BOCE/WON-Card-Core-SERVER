package com.woorifisa.won_card_core_server.domain.reward.dto.response;

import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;

import java.time.LocalDateTime;
import java.util.List;

public record RewardLedgerResponse(
        int baseYear,
        Long totalAccumulatedAmount,
        List<RewardLedgerItem> ledgers
) {
    public record RewardLedgerItem(
            Long pointLedgerId,
            String baseMonth,
            Long pointAmount,
            String type,
            String sweepStatus,
            String sweepFailureCode,
            String sweepFailureMessage,
            LocalDateTime occurredAt
    ) {

        public static RewardLedgerItem from(CardPointLedger ledger) {
            return new RewardLedgerItem(
                    ledger.getPointLedgerId(),
                    ledger.getBaseMonth(),
                    ledger.getDisplayPointAmount(),
                    ledger.getRewardProcessStatus().name(),
                    ledger.getSweepStatus().name(),
                    ledger.getSweepFailureCode(),
                    ledger.getSweepFailureMessage(),
                    ledger.getOccurredAt()
            );
        }
    }
}
