package com.woorifisa.won_card_core_server.domain.card.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.woorifisa.won_card_core_server.domain.card.dto.response.CardInfoResponse;
import com.woorifisa.won_card_core_server.domain.card.model.Card;
import com.woorifisa.won_card_core_server.domain.card.model.CardStatus;
import com.woorifisa.won_card_core_server.domain.card.model.CardUser;
import com.woorifisa.won_card_core_server.domain.card.model.CardUserStatus;
import com.woorifisa.won_card_core_server.domain.card.model.Gender;
import com.woorifisa.won_card_core_server.domain.card.repository.CardRepository;
import com.woorifisa.won_card_core_server.domain.card.repository.CardUserRepository;
import com.woorifisa.won_card_core_server.domain.performance.model.CardPerformance;
import com.woorifisa.won_card_core_server.domain.performance.repository.CardPerformanceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class CardInfoServiceTest {

    private static final UUID USER_UUID =
            UUID.fromString("0a31e4b1-2b1d-4b5e-8b82-0fb48e502111");
    private static final UUID CARD_USER_UUID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CARD_UUID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private CardUserRepository cardUserRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardPerformanceRepository cardPerformanceRepository;

    @InjectMocks
    private CardInfoService cardInfoService;

    @Test
    @DisplayName("userUuid로 카드 사용자를 찾지 못하면 카드 없음 응답을 반환한다")
    void getCardInfoWhenCardUserNotFound() {
        // given
        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.empty());

        // when
        CardInfoResponse response = cardInfoService.getCardInfo(USER_UUID);

        // then
        assertThat(response.hasCard()).isFalse();
        assertThat(response.cardUuid()).isNull();
        assertThat(response.cardNoDisplay()).isNull();
        assertThat(response.cardStatus()).isNull();
        assertThat(response.usageSummary()).isNull();

        then(cardRepository).shouldHaveNoInteractions();
        then(cardPerformanceRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("카드 사용자는 있지만 발급 카드가 없으면 카드 없음 응답을 반환한다")
    void getCardInfoWhenCardNotFound() {
        // given
        CardUser cardUser = newCardUser();

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(cardUser));
        given(cardRepository.findFirstByCardUserCardUserUuidOrderByIssuedAtDesc(CARD_USER_UUID))
                .willReturn(Optional.empty());

        // when
        CardInfoResponse response = cardInfoService.getCardInfo(USER_UUID);

        // then
        assertThat(response.hasCard()).isFalse();
        assertThat(response.usageSummary()).isNull();

        then(cardRepository).should().findFirstByCardUserCardUserUuidOrderByIssuedAtDesc(CARD_USER_UUID);
        then(cardPerformanceRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("카드와 당월 실적이 있으면 카드 정보와 당월 사용 금액을 반환한다")
    void getCardInfoWithPerformance() {
        // given
        CardUser cardUser = newCardUser();
        Card card = newCard(cardUser);
        CardPerformance performance = newCardPerformance(BigDecimal.valueOf(1245000));
        String baseMonth = YearMonth.now().toString();

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(cardUser));
        given(cardRepository.findFirstByCardUserCardUserUuidOrderByIssuedAtDesc(CARD_USER_UUID))
                .willReturn(Optional.of(card));
        given(cardPerformanceRepository.findByCardUserUuidAndBaseMonth(CARD_USER_UUID, baseMonth))
                .willReturn(Optional.of(performance));

        // when
        CardInfoResponse response = cardInfoService.getCardInfo(USER_UUID);

        // then
        assertThat(response.hasCard()).isTrue();
        assertThat(response.cardUuid()).isEqualTo(CARD_UUID);
        assertThat(response.cardNoDisplay()).isEqualTo("****-****-****-1234");
        assertThat(response.cardStatus()).isEqualTo("ACTIVE");
        assertThat(response.usageSummary().currentMonthUsageAmount()).isEqualTo(1245000L);

        then(cardPerformanceRepository).should()
                .findByCardUserUuidAndBaseMonth(CARD_USER_UUID, baseMonth);
    }

    @Test
    @DisplayName("카드는 있지만 당월 실적이 없으면 당월 사용 금액은 0으로 반환한다")
    void getCardInfoWithoutPerformance() {
        // given
        CardUser cardUser = newCardUser();
        Card card = newCard(cardUser);
        String baseMonth = YearMonth.now().toString();

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(cardUser));
        given(cardRepository.findFirstByCardUserCardUserUuidOrderByIssuedAtDesc(CARD_USER_UUID))
                .willReturn(Optional.of(card));
        given(cardPerformanceRepository.findByCardUserUuidAndBaseMonth(CARD_USER_UUID, baseMonth))
                .willReturn(Optional.empty());

        // when
        CardInfoResponse response = cardInfoService.getCardInfo(USER_UUID);

        // then
        assertThat(response.hasCard()).isTrue();
        assertThat(response.usageSummary().currentMonthUsageAmount()).isZero();

        then(cardPerformanceRepository).should()
                .findByCardUserUuidAndBaseMonth(CARD_USER_UUID, baseMonth);
    }

    @Test
    @DisplayName("당월 사용 금액에 소수점이 있으면 소수점 이하는 버린다")
    void getCardInfoTruncatesDecimalUsageAmount() {
        // given
        CardUser cardUser = newCardUser();
        Card card = newCard(cardUser);
        CardPerformance performance = newCardPerformance(BigDecimal.valueOf(1245000.9999));
        String baseMonth = YearMonth.now().toString();

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(cardUser));
        given(cardRepository.findFirstByCardUserCardUserUuidOrderByIssuedAtDesc(CARD_USER_UUID))
                .willReturn(Optional.of(card));
        given(cardPerformanceRepository.findByCardUserUuidAndBaseMonth(CARD_USER_UUID, baseMonth))
                .willReturn(Optional.of(performance));

        // when
        CardInfoResponse response = cardInfoService.getCardInfo(USER_UUID);

        // then
        assertThat(response.usageSummary().currentMonthUsageAmount()).isEqualTo(1245000L);
    }

    private CardUser newCardUser() {
        return CardUser.builder()
                .cardUserUuid(CARD_USER_UUID)
                .userUuid(USER_UUID)
                .userNameEnc("userNameEnc")
                .birthDateEnc("birthDateEnc")
                .gender(Gender.M)
                .ciHash("ciHash")
                .nationality("KR")
                .userStatus(CardUserStatus.ACTIVE)
                .isAgree(true)
                .telEnc("telEnc")
                .emailEnc("emailEnc")
                .addressEnc("addressEnc")
                .build();
    }

    private Card newCard(CardUser cardUser) {
        return Card.builder()
                .cardUuid(CARD_UUID)
                .cardUser(cardUser)
                .cardNoToken("cardNoToken")
                .cardNoDisplay("****-****-****-1234")
                .cardStatus(CardStatus.ACTIVE)
                .issuedAt(LocalDateTime.of(2026, 5, 26, 12, 0))
                .expiredAt(LocalDateTime.of(2031, 5, 26, 12, 0))
                .totalLimitAmount(BigDecimal.ZERO)
                .availableLimitAmount(BigDecimal.ZERO)
                .build();
    }

    private CardPerformance newCardPerformance(BigDecimal currentMonthSpendAmount) {
        return CardPerformance.builder()
                .performanceId(1L)
                .userUuid(USER_UUID)
                .cardUserUuid(CARD_USER_UUID)
                .baseMonth(YearMonth.now().toString())
                .previousMonthSpendAmount(BigDecimal.ZERO)
                .currentMonthSpendAmount(currentMonthSpendAmount)
                .rewardRate(BigDecimal.ZERO)
                .rewardPointAmount(BigDecimal.ZERO)
                .limitApplyStatus("NONE")
                .performanceStatus("CONFIRMED")
                .build();
    }
}