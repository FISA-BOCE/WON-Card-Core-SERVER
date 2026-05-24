package com.woorifisa.won_card_core_server.domain.card.dto.response;

import com.woorifisa.won_card_core_server.domain.card.model.Card;
import com.woorifisa.won_card_core_server.domain.card.model.CardStatus;

import java.time.LocalDateTime;

public record CardApplicationResponse(
        String cardUuid,
        String cardNoDisplay,
        LocalDateTime issuedAt,
        CardStatus cardStatus
) {

    public static CardApplicationResponse from(Card card) {
        return new CardApplicationResponse(
                card.getCardUuid(),
                card.getCardNoDisplay(),
                card.getIssuedAt(),
                card.getCardStatus()
        );
    }
}
