package com.woorifisa.won_card_core_server.domain.reward.service.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorifisa.won_card_core_server.domain.reward.dto.payload.SweepRequestOutboxPayload;
import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SweepRequestPayloadFactory {

    private final ObjectMapper objectMapper;

    public String create(CardPointLedger ledger, Long pointAmount) {
        SweepRequestOutboxPayload payload = new SweepRequestOutboxPayload(
                "SWEEP_REQUESTED",
                "CARD-SWEEP-" + ledger.getPointLedgerId(),
                ledger.getIdempotencyKey(),
                ledger.getSweepRequestId(),
                ledger.getPointLedgerId(),
                ledger.getCardUserUuid(),
                ledger.getPerformanceId(),
                ledger.getBaseMonth(),
                pointAmount,
                pointAmount
        );

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("스윕 요청 payload 생성에 실패했습니다.", e);
        }
    }
}
