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
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("previousMonth 다음 달 baseMonth로 전월 실적을 조회하고 기준 충족 응답을 반환한다")
    void getPreviousPerformanceSatisfied() {
        // given
        CardUser cardUser = newCardUser();
        CardPerformance performance = newCardPerformance(
                "2026-05",
                BigDecimal.valueOf(820000),
                BigDecimal.valueOf(8200)
        );

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(cardUser));
        given(cardPerformanceRepository.findByUserUuidAndBaseMonth(USER_UUID, "2026-05"))
                .willReturn(Optional.of(performance));

        // when
        PreviousPerformanceResponse response =
                cardPerformanceService.getPreviousPerformance(USER_UUID, "2026-04");

        // then
        assertThat(response.baseMonth()).isEqualTo("2026-05");
        assertThat(response.rewardStatus()).isEqualTo("기준 충족");
        assertThat(response.previousMonthSpendAmount()).isEqualTo(820000L);
        assertThat(response.detail().totalSpendAmount()).isEqualTo(820000L);
        assertThat(response.detail().rewardPointAmount()).isEqualTo(8200L);

        then(cardPerformanceRepository).should().findByUserUuidAndBaseMonth(USER_UUID, "2026-05");
    }

    @Test
    @DisplayName("전월 실적 금액이 0이면 기준 미달 응답을 반환한다")
    void getPreviousPerformanceNotSatisfied() {
        // given
        CardUser cardUser = newCardUser();
        CardPerformance performance = newCardPerformance(
                "2026-05",
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(cardUser));
        given(cardPerformanceRepository.findByUserUuidAndBaseMonth(USER_UUID, "2026-05"))
                .willReturn(Optional.of(performance));

        // when
        PreviousPerformanceResponse response =
                cardPerformanceService.getPreviousPerformance(USER_UUID, "2026-04");

        // then
        assertThat(response.rewardStatus()).isEqualTo("기준 미달");
        assertThat(response.previousMonthSpendAmount()).isZero();
        assertThat(response.detail().rewardPointAmount()).isZero();
    }

    @Test
    @DisplayName("previousMonth가 null이면 서울 시간 기준 전월을 사용한다")
    void getPreviousPerformanceWhenPreviousMonthNull() {
        // given
        YearMonth previousMonth = YearMonth.now(SEOUL_ZONE_ID).minusMonths(1);
        String targetBaseMonth = previousMonth.plusMonths(1).toString();
        CardUser cardUser = newCardUser();
        CardPerformance performance = newCardPerformance(
                targetBaseMonth,
                BigDecimal.valueOf(820000),
                BigDecimal.valueOf(8200)
        );

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(cardUser));
        given(cardPerformanceRepository.findByUserUuidAndBaseMonth(USER_UUID, targetBaseMonth))
                .willReturn(Optional.of(performance));

        // when
        PreviousPerformanceResponse response =
                cardPerformanceService.getPreviousPerformance(USER_UUID, null);

        // then
        assertThat(response.baseMonth()).isEqualTo(targetBaseMonth);
        then(cardPerformanceRepository).should().findByUserUuidAndBaseMonth(USER_UUID, targetBaseMonth);
    }

    @Test
    @DisplayName("previousMonth가 YYYY-MM 형식이 아니면 예외를 던진다")
    void getPreviousPerformanceInvalidMonth() {
        // when & then
        assertThatThrownBy(() -> cardPerformanceService.getPreviousPerformance(USER_UUID, "2026-4"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(CardPerformanceErrorCode.INVALID_PERFORMANCE_MONTH));

        then(cardUserRepository).shouldHaveNoInteractions();
        then(cardPerformanceRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("card_user가 없으면 고객 정보 없음 예외를 던진다")
    void getPreviousPerformanceCardUserNotFound() {
        // given
        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cardPerformanceService.getPreviousPerformance(USER_UUID, "2026-04"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(CardPerformanceErrorCode.CARD_USER_NOT_FOUND));
    }

    @Test
    @DisplayName("userUuid가 null이면 요청 형식 오류 예외를 던지고 저장소를 호출하지 않는다")
    void getPreviousPerformanceUserUuidNull() {
        // when & then
        assertThatThrownBy(() -> cardPerformanceService.getPreviousPerformance(null, "2026-04"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(CommonErrorCode.INVALID_REQUEST));

        then(cardUserRepository).shouldHaveNoInteractions();
        then(cardPerformanceRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("card_performance가 없으면 실적 정보 없음 예외를 던진다")
    void getPreviousPerformanceNotFound() {
        // given
        CardUser cardUser = newCardUser();

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(cardUser));
        given(cardPerformanceRepository.findByUserUuidAndBaseMonth(USER_UUID, "2026-05"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cardPerformanceService.getPreviousPerformance(USER_UUID, "2026-04"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(CardPerformanceErrorCode.PERFORMANCE_NOT_FOUND));
    }

    @Test
    @DisplayName("전월 실적 금액이 null이면 금액 형식 오류 예외를 던진다")
    void getPreviousPerformanceInvalidAmountWhenPreviousMonthSpendAmountNull() {
        // given
        CardUser cardUser = newCardUser();
        CardPerformance performance = newCardPerformance(
                "2026-05",
                null,
                BigDecimal.ZERO
        );

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(cardUser));
        given(cardPerformanceRepository.findByUserUuidAndBaseMonth(USER_UUID, "2026-05"))
                .willReturn(Optional.of(performance));

        // when & then
        assertThatThrownBy(() -> cardPerformanceService.getPreviousPerformance(USER_UUID, "2026-04"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(CardPerformanceErrorCode.INVALID_PERFORMANCE_AMOUNT));
    }

    @Test
    @DisplayName("전월 실적 금액이 음수이면 금액 형식 오류 예외를 던진다")
    void getPreviousPerformanceInvalidAmountWhenPreviousMonthSpendAmountNegative() {
        // given
        CardUser cardUser = newCardUser();
        CardPerformance performance = newCardPerformance(
                "2026-05",
                BigDecimal.valueOf(-1),
                BigDecimal.ZERO
        );

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(cardUser));
        given(cardPerformanceRepository.findByUserUuidAndBaseMonth(USER_UUID, "2026-05"))
                .willReturn(Optional.of(performance));

        // when & then
        assertThatThrownBy(() -> cardPerformanceService.getPreviousPerformance(USER_UUID, "2026-04"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(CardPerformanceErrorCode.INVALID_PERFORMANCE_AMOUNT));
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
