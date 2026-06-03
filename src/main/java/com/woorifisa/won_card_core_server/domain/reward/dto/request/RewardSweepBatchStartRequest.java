package com.woorifisa.won_card_core_server.domain.reward.dto.request;

public record RewardSweepBatchStartRequest(
        String baseMonth,
        Integer chunkSize
) {
}
