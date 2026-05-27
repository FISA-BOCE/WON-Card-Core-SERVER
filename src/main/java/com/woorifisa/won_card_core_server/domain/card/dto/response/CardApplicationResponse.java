package com.woorifisa.won_card_core_server.domain.card.dto.response;

import com.woorifisa.won_card_core_server.domain.card.model.Card;

import java.time.LocalDateTime;
import java.util.UUID;

public record CardApplicationResponse(
        UUID cardUuid,
        String cardNoDisplay,
        LocalDateTime issuedAt,
        String cardStatus
) {

    public static CardApplicationResponse from(Card card) {
        return new CardApplicationResponse(
                card.getCardUuid(),
                card.getCardNoDisplay(),
                card.getIssuedAt(),
                card.getCardStatus().name()
        );
    }
}
