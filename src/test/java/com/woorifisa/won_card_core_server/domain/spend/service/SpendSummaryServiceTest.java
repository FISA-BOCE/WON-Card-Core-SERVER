package com.woorifisa.won_card_core_server.domain.spend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.woorifisa.won_card_core_server.domain.card.model.CardUser;
import com.woorifisa.won_card_core_server.domain.card.model.CardUserStatus;
import com.woorifisa.won_card_core_server.domain.card.model.Gender;
import com.woorifisa.won_card_core_server.domain.card.repository.CardRepository;
import com.woorifisa.won_card_core_server.domain.card.repository.CardUserRepository;
import com.woorifisa.won_card_core_server.domain.performance.model.CardPerformance;
import com.woorifisa.won_card_core_server.domain.performance.repository.CardPerformanceRepository;
import com.woorifisa.won_card_core_server.domain.spend.dto.response.CurrentSpendAmountResponse;
import com.woorifisa.won_card_core_server.domain.spend.exception.code.SpendErrorCode;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class SpendSummaryServiceTest {

    private static final UUID USER_UUID =
            UUID.fromString("0a31e4b1-2b1d-4b5e-8b82-0fb48e502111");
    private static final UUID CARD_USER_UUID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private CardUserRepository cardUserRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardPerformanceRepository cardPerformanceRepository;

    @InjectMocks
    private SpendSummaryService spendSummaryService;

    @Test
    @DisplayName("당월 이용 금액과 다음 구간, 예상 리워드를 계산한다")
    void getSpendSummary() {
        // given
        String baseMonth = YearMonth.now().toString();
        CardUser cardUser = newCardUser();
        CardPerformance performance = newCardPerformance(BigDecimal.valueOf(1_245_000));

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(cardUser));
        given(cardRepository.existsByCardUserUuid(CARD_USER_UUID)).willReturn(true);
        given(cardPerformanceRepository.findByCardUserUuidAndBaseMonth(CARD_USER_UUID, baseMonth))
                .willReturn(Optional.of(performance));

        // when
        CurrentSpendAmountResponse response = spendSummaryService.getSpendSummary(USER_UUID);

        // then
        assertThat(response.hasCurrentSpendAmount()).isTrue();
        assertThat(response.baseMonth()).isEqualTo(baseMonth);
        assertThat(response.currentSpendAmount()).isEqualTo(1_245_000L);
        assertThat(response.currentRewardRate()).isEqualByComparingTo("1.0");
        assertThat(response.nextPerformanceStatus()).isEqualTo("3");
        assertThat(response.amountRemainingUntilNextPerformance()).isEqualTo(255_000L);
        assertThat(response.nextRewardRate()).isEqualByComparingTo("1.2");
        assertThat(response.expectedReward().targetSpendAmount()).isEqualTo(1_245_000L);
        assertThat(response.expectedReward().rewardRate()).isEqualByComparingTo("1.0");
        assertThat(response.expectedReward().expectedRewardAmount()).isEqualTo(12_450L);
    }

    @ParameterizedTest(name = "currentSpendAmount={0}")
    @CsvSource({
            "-10000,1,0.7,2,510000,1.0,0",
            "0,1,0.7,2,500000,1.0,0",
            "499999,1,0.7,2,1,1.0,3499",
            "500000,2,1.0,3,1000000,1.2,5000",
            "1499999,2,1.0,3,1,1.2,14999",
            "1500000,3,1.2,3,0,1.2,18000",
            "16666667,3,1.2,3,0,1.2,40000"
    })
    @DisplayName("구간별 경곗값에 따라 적립률, 다음 구간, 남은 금액, 예상 리워드를 계산한다")
    void getSpendSummaryByPerformanceRange(
            Long currentSpendAmount,
            String expectedCurrentPerformanceStatus,
            String expectedCurrentRewardRate,
            String expectedNextPerformanceStatus,
            Long expectedAmountRemainingUntilNextPerformance,
            String expectedNextRewardRate,
            Long expectedRewardAmount
    ) {
        // given
        String baseMonth = YearMonth.now().toString();
        CardUser cardUser = newCardUser();
        CardPerformance performance = newCardPerformance(BigDecimal.valueOf(currentSpendAmount));

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(cardUser));
        given(cardRepository.existsByCardUserUuid(CARD_USER_UUID)).willReturn(true);
        given(cardPerformanceRepository.findByCardUserUuidAndBaseMonth(CARD_USER_UUID, baseMonth))
                .willReturn(Optional.of(performance));

        // when
        CurrentSpendAmountResponse response = spendSummaryService.getSpendSummary(USER_UUID);

        // then
        assertThat(response.currentRewardRate()).isEqualByComparingTo(expectedCurrentRewardRate);
        assertThat(response.nextPerformanceStatus()).isEqualTo(expectedNextPerformanceStatus);
        assertThat(response.amountRemainingUntilNextPerformance())
                .isEqualTo(expectedAmountRemainingUntilNextPerformance);
        assertThat(response.nextRewardRate()).isEqualByComparingTo(expectedNextRewardRate);
        assertThat(response.expectedReward().rewardRate()).isEqualByComparingTo(expectedCurrentRewardRate);
        assertThat(response.expectedReward().expectedRewardAmount()).isEqualTo(expectedRewardAmount);
        assertThat(calculateExpectedCurrentPerformanceStatus(response.currentSpendAmount()))
                .isEqualTo(expectedCurrentPerformanceStatus);
    }

    @Test
    @DisplayName("card_user가 없으면 고객 정보 없음 예외를 던진다")
    void getSpendSummaryWhenCardUserNotFound() {
        // given
        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> spendSummaryService.getSpendSummary(USER_UUID))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(SpendErrorCode.CARD_USER_NOT_FOUND));
    }

    @Test
    @DisplayName("card가 없으면 고객 정보 없음 예외를 던진다")
    void getSpendSummaryWhenCardNotFound() {
        // given
        CardUser cardUser = newCardUser();

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(cardUser));
        given(cardRepository.existsByCardUserUuid(CARD_USER_UUID)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> spendSummaryService.getSpendSummary(USER_UUID))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(SpendErrorCode.CARD_USER_NOT_FOUND));
    }

    @Test
    @DisplayName("card_performance가 없으면 이용 금액 정보 없음 예외를 던진다")
    void getSpendSummaryWhenPerformanceNotFound() {
        // given
        String baseMonth = YearMonth.now().toString();
        CardUser cardUser = newCardUser();

        given(cardUserRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(cardUser));
        given(cardRepository.existsByCardUserUuid(CARD_USER_UUID)).willReturn(true);
        given(cardPerformanceRepository.findByCardUserUuidAndBaseMonth(CARD_USER_UUID, baseMonth))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> spendSummaryService.getSpendSummary(USER_UUID))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(SpendErrorCode.CURRENT_SPEND_AMOUNT_NOT_FOUND));
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

    private CardPerformance newCardPerformance(BigDecimal currentMonthSpendAmount) {
        return CardPerformance.builder()
                .userUuid(USER_UUID)
                .cardUserUuid(CARD_USER_UUID)
                .baseMonth(YearMonth.now().toString())
                .previousMonthSpendAmount(BigDecimal.ZERO)
                .currentMonthSpendAmount(currentMonthSpendAmount)
                .rewardRate(BigDecimal.ZERO)
                .rewardPointAmount(BigDecimal.ZERO)
                .performanceStatus("2")
                .build();
    }

    private String calculateExpectedCurrentPerformanceStatus(Long currentSpendAmount) {
        if (currentSpendAmount < 500_000L) {
            return "1";
        }
        if (currentSpendAmount < 1_500_000L) {
            return "2";
        }
        return "3";
    }
}
