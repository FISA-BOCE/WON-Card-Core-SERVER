package com.woorifisa.won_card_core_server.domain.card.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.woorifisa.won_card_core_server.domain.card.model.Card;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CardInfoResponse(
        Boolean hasCard,
        UUID cardUuid,
        String cardNoDisplay,
        String cardStatus,
        UsageSummary usageSummary
) {

    public static CardInfoResponse noCard() {
        return new CardInfoResponse(
                false,
                null,
                null,
                null,
                null
        );
    }

    public static CardInfoResponse of(
            Card card,
            Long currentMonthUsageAmount
    ) {
        return new CardInfoResponse(
                true,
                card.getCardUuid(),
                card.getCardNoDisplay(),
                card.getCardStatus().name(),
                new UsageSummary(currentMonthUsageAmount)
        );
    }

    public record UsageSummary(
            Long currentMonthUsageAmount
    ) {
    }
}
