package com.woorifisa.won_card_core_server.domain.reward.dto.response;

import com.woorifisa.won_card_core_server.domain.reward.model.SweepRequestOutbox;

import java.util.UUID;

public record SweepRequestOutboxResponse(
        Long outboxId,
        String idempotencyKey,
        UUID cardUserUuid,
        String payload
) {
    public static SweepRequestOutboxResponse from(SweepRequestOutbox outbox) {
        return new SweepRequestOutboxResponse(
                outbox.getOutboxId(),
                outbox.getIdempotencyKey(),
                outbox.getCardUserUuid(),
                outbox.getPayload()
        );
    }
}
