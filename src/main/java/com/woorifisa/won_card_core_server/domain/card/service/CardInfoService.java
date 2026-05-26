package com.woorifisa.won_card_core_server.domain.card.service;

import com.woorifisa.won_card_core_server.domain.card.dto.response.CardInfoResponse;
import com.woorifisa.won_card_core_server.domain.card.model.Card;
import com.woorifisa.won_card_core_server.domain.card.model.CardUser;
import com.woorifisa.won_card_core_server.domain.card.repository.CardRepository;
import com.woorifisa.won_card_core_server.domain.card.repository.CardUserRepository;
import com.woorifisa.won_card_core_server.domain.performance.model.CardPerformance;
import com.woorifisa.won_card_core_server.domain.performance.repository.CardPerformanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardInfoService {

    private final CardUserRepository cardUserRepository;
    private final CardRepository cardRepository;
    private final CardPerformanceRepository cardPerformanceRepository;

    public CardInfoResponse getCardInfo(UUID userUuid) {
        return cardUserRepository.findByUserUuid(userUuid)
                .map(CardUser::getCardUserUuid)
                .flatMap(cardRepository::findFirstByCardUserCardUserUuidOrderByIssuedAtDesc)
                .map(this::createCardInfoResponse)
                .orElseGet(CardInfoResponse::noCard);
    }

    private CardInfoResponse createCardInfoResponse(Card card) {
        String baseMonth = YearMonth.now().toString();
        Long currentMonthUsageAmount = cardPerformanceRepository
                .findByCardUserUuidAndBaseMonth(card.getCardUser().getCardUserUuid(), baseMonth)
                .map(CardPerformance::getCurrentMonthSpendAmount)
                .map(this::toLong)
                .orElse(0L);

        return CardInfoResponse.of(card, currentMonthUsageAmount);
    }

    private Long toLong(BigDecimal amount) {
        if (amount == null) {
            return 0L;
        }

        return amount.setScale(0, RoundingMode.DOWN).longValue();
    }
}