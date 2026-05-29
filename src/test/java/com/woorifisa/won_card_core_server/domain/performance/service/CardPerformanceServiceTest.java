package com.woorifisa.won_card_core_server.domain.performance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.woorifisa.won_card_core_server.domain.card.model.CardUser;
import com.woorifisa.won_card_core_server.domain.card.model.CardUserStatus;
import com.woorifisa.won_card_core_server.domain.card.model.Gender;
import com.woorifisa.won_card_core_server.domain.card.repository.CardUserRepository;
import com.woorifisa.won_card_core_server.domain.performance.dto.response.PreviousPerformanceResponse;
import com.woorifisa.won_card_core_server.domain.performance.exception.code.CardPerformanceErrorCode;
import com.woorifisa.won_card_core_server.domain.performance.model.CardPerformance;
import com.woorifisa.won_card_core_server.domain.performance.repository.CardPerformanceRepository;
import com.woorifisa.won_card_core_server.global.exception.code.CommonErrorCode;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class CardPerformanceServiceTest {

    private static final UUID USER_UUID =
            UUID.fromString("0a31e4b1-2b1d-4b5e-8b82-0fb48e502111");
    private static final UUID CARD_USER_UUID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");

    @Mock
    private CardUserRepository cardUserRepository;

    @Mock
    private CardPerformanceRepository cardPerformanceRepository;

    @InjectMocks
    private CardPerformanceService cardPerformanceService;

    @Test
    void getPreviousPerformanceSatisfied() {
        String baseMonth = currentBaseMonth();
        CardPerformance performance = newCardPerformance(
                baseMonth,
                BigDecimal.valueOf(820000),
                BigDecimal.valueOf(8200)
        );

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(newCardUser()));
        given(cardPerformanceRepository.findByUserUuidAndBaseMonth(USER_UUID, baseMonth))
                .willReturn(Optional.of(performance));

        PreviousPerformanceResponse response = cardPerformanceService.getPreviousPerformance(USER_UUID);

        assertThat(response.baseMonth()).isEqualTo(baseMonth);
        assertThat(response.rewardStatus()).isEqualTo("기준 충족");
        assertThat(response.previousMonthSpendAmount()).isEqualTo(820000L);
        assertThat(response.rewardPointAmount()).isEqualTo(8200L);
        assertThat(response.rewardRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.performanceStatus()).isEqualTo("1");
        then(cardPerformanceRepository).should().findByUserUuidAndBaseMonth(USER_UUID, baseMonth);
    }

    @Test
    void getPreviousPerformanceNotSatisfied() {
        String baseMonth = currentBaseMonth();
        CardPerformance performance = newCardPerformance(baseMonth, BigDecimal.ZERO, BigDecimal.ZERO);

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(newCardUser()));
        given(cardPerformanceRepository.findByUserUuidAndBaseMonth(USER_UUID, baseMonth))
                .willReturn(Optional.of(performance));

        PreviousPerformanceResponse response = cardPerformanceService.getPreviousPerformance(USER_UUID);

        assertThat(response.rewardStatus()).isEqualTo("기준 미달");
        assertThat(response.previousMonthSpendAmount()).isZero();
        assertThat(response.rewardPointAmount()).isZero();
    }

    @Test
    void getPreviousPerformanceCardUserNotFound() {
        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> cardPerformanceService.getPreviousPerformance(USER_UUID))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(CardPerformanceErrorCode.CARD_USER_NOT_FOUND));
    }

    @Test
    void getPreviousPerformanceUserUuidNull() {
        assertThatThrownBy(() -> cardPerformanceService.getPreviousPerformance(null))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(CommonErrorCode.INVALID_REQUEST));

        then(cardUserRepository).shouldHaveNoInteractions();
        then(cardPerformanceRepository).shouldHaveNoInteractions();
    }

    @Test
    void getPreviousPerformanceNotFound() {
        String baseMonth = currentBaseMonth();

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(newCardUser()));
        given(cardPerformanceRepository.findByUserUuidAndBaseMonth(USER_UUID, baseMonth))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> cardPerformanceService.getPreviousPerformance(USER_UUID))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(CardPerformanceErrorCode.PERFORMANCE_NOT_FOUND));
    }

    @Test
    void getPreviousPerformanceInvalidAmountWhenPreviousMonthSpendAmountNull() {
        String baseMonth = currentBaseMonth();
        CardPerformance performance = newCardPerformance(baseMonth, null, BigDecimal.ZERO);

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(newCardUser()));
        given(cardPerformanceRepository.findByUserUuidAndBaseMonth(USER_UUID, baseMonth))
                .willReturn(Optional.of(performance));

        assertThatThrownBy(() -> cardPerformanceService.getPreviousPerformance(USER_UUID))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(CardPerformanceErrorCode.INVALID_PERFORMANCE_AMOUNT));
    }

    @Test
    void getPreviousPerformanceInvalidAmountWhenPreviousMonthSpendAmountNegative() {
        String baseMonth = currentBaseMonth();
        CardPerformance performance = newCardPerformance(baseMonth, BigDecimal.valueOf(-1), BigDecimal.ZERO);

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(newCardUser()));
        given(cardPerformanceRepository.findByUserUuidAndBaseMonth(USER_UUID, baseMonth))
                .willReturn(Optional.of(performance));

        assertThatThrownBy(() -> cardPerformanceService.getPreviousPerformance(USER_UUID))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(CardPerformanceErrorCode.INVALID_PERFORMANCE_AMOUNT));
    }

    private String currentBaseMonth() {
        return YearMonth.now(SEOUL_ZONE_ID).toString();
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

    private CardPerformance newCardPerformance(
            String baseMonth,
            BigDecimal previousMonthSpendAmount,
            BigDecimal rewardPointAmount
    ) {
        return CardPerformance.builder()
                .userUuid(USER_UUID)
                .cardUserUuid(CARD_USER_UUID)
                .baseMonth(baseMonth)
                .previousMonthSpendAmount(previousMonthSpendAmount)
                .currentMonthSpendAmount(BigDecimal.ZERO)
                .rewardRate(BigDecimal.ZERO)
                .rewardPointAmount(rewardPointAmount)
                .performanceStatus("1")
                .build();
    }
}
