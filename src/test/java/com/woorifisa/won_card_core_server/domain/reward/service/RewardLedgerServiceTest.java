package com.woorifisa.won_card_core_server.domain.reward.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.woorifisa.won_card_core_server.domain.performance.exception.code.CardPerformanceErrorCode;
import com.woorifisa.won_card_core_server.domain.performance.model.CardPerformance;
import com.woorifisa.won_card_core_server.domain.performance.repository.CardPerformanceRepository;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardLedgerDetailResponse;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardLedgerResponse;
import com.woorifisa.won_card_core_server.domain.reward.exception.code.RewardErrorCode;
import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.RewardProcessStatus;
import com.woorifisa.won_card_core_server.domain.reward.repository.CardPointLedgerRepository;
import com.woorifisa.won_card_core_server.domain.reward.service.validator.RewardLedgerValidator;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RewardLedgerServiceTest {

    @Spy
    private RewardLedgerValidator rewardLedgerValidator;

    private static final UUID USER_UUID =
            UUID.fromString("0a31e4b1-2b1d-4b5e-8b82-0fb48e502111");

    private static final UUID CARD_USER_UUID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final UUID POINT_UUID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private CardPointLedgerRepository cardPointLedgerRepository;

    @Mock
    private CardPerformanceRepository cardPerformanceRepository;

    @InjectMocks
    private RewardLedgerService rewardLedgerService;

    @Test
    @DisplayName("type이 없으면 전체 리워드 내역을 조회한다")
    void getRewardLedgerAll() throws Exception {
        // given
        CardPointLedger earnLedger = newCardPointLedger(
                1001L,
                RewardProcessStatus.EARN,
                BigDecimal.valueOf(12450),
                null,
                LocalDateTime.of(2026, 5, 7, 14, 32)
        );

        CardPointLedger holdLedger = newCardPointLedger(
                1002L,
                RewardProcessStatus.EARN,
                BigDecimal.valueOf(5000),
                null,
                LocalDateTime.of(2026, 5, 6, 14, 32)
        );

        given(cardPointLedgerRepository.sumEarnAmount(
                eq(CARD_USER_UUID), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(BigDecimal.valueOf(17450));

        given(cardPointLedgerRepository.findRewardLedgers(
                eq(CARD_USER_UUID), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(List.of(earnLedger, holdLedger));

        // when
        RewardLedgerResponse response = rewardLedgerService.getRewardLedger(CARD_USER_UUID, null);

        // then
        assertThat(response.baseYear()).isEqualTo(Year.now().getValue());
        assertThat(response.totalAccumulatedAmount()).isEqualTo(17450L);
        assertThat(response.ledgers()).hasSize(2);

        assertThat(response.ledgers().get(0).pointLedgerId()).isEqualTo(1001L);
        assertThat(response.ledgers().get(0).pointAmount()).isEqualTo(12450L);
        assertThat(response.ledgers().get(0).type()).isEqualTo("EARN");

        assertThat(response.ledgers().get(1).pointLedgerId()).isEqualTo(1002L);
        assertThat(response.ledgers().get(1).pointAmount()).isEqualTo(5000);
        assertThat(response.ledgers().get(1).type()).isEqualTo("EARN");

        then(cardPointLedgerRepository).should().findRewardLedgers(
                eq(CARD_USER_UUID), any(LocalDateTime.class), any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("type이 EARN이면 적립 완료 내역만 조회한다")
    void getRewardLedgerEarn() throws Exception {
        // given
        CardPointLedger earnLedger = newCardPointLedger(
                1001L, RewardProcessStatus.EARN, BigDecimal.valueOf(12450), null,
                LocalDateTime.of(2026, 5, 7, 14, 32)
        );

        given(cardPointLedgerRepository.sumEarnAmount(
                eq(CARD_USER_UUID), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(BigDecimal.valueOf(12450));

        given(cardPointLedgerRepository.findRewardLedgersByStatus(
                eq(CARD_USER_UUID), eq(RewardProcessStatus.EARN), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(List.of(earnLedger));

        // when
        RewardLedgerResponse response = rewardLedgerService.getRewardLedger(CARD_USER_UUID, "EARN");

        // then
        assertThat(response.totalAccumulatedAmount()).isEqualTo(12450L);
        assertThat(response.ledgers()).hasSize(1);
        assertThat(response.ledgers().get(0).type()).isEqualTo("EARN");

        then(cardPointLedgerRepository).should().findRewardLedgersByStatus(
                eq(CARD_USER_UUID), eq(RewardProcessStatus.EARN), any(LocalDateTime.class), any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("유효하지 않은 type이면 예외가 발생한다")
    void getRewardLedgerInvalidType() {
        assertThatThrownBy(() -> rewardLedgerService.getRewardLedger(CARD_USER_UUID, "BAD"))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode())
                            .isEqualTo(RewardErrorCode.INVALID_REWARD_LEDGER_TYPE);
                });
    }

    @Test
    @DisplayName("리워드 내역이 없으면 빈 목록을 반환한다")
    void getRewardLedgerEmpty() {
        // given
        given(cardPointLedgerRepository.sumEarnAmount(
                eq(CARD_USER_UUID), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(BigDecimal.ZERO);

        given(cardPointLedgerRepository.findRewardLedgers(eq(CARD_USER_UUID), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(List.of());

        // when
        RewardLedgerResponse response = rewardLedgerService.getRewardLedger(CARD_USER_UUID, null);

        // then
        assertThat(response.baseYear()).isEqualTo(Year.now().getValue());
        assertThat(response.totalAccumulatedAmount()).isEqualTo(0L);
        assertThat(response.ledgers()).isEmpty();

        then(cardPointLedgerRepository).should().findRewardLedgers(
                eq(CARD_USER_UUID),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("적립 리워드 내역 상세를 조회한다")
    void getRewardLedgerDetailEarn() throws Exception {
        // given
        Long pointLedgerId = 1L;
        Long performanceId = 30001L;

        CardPointLedger pointLedger = newCardPointLedger(pointLedgerId, performanceId,
                RewardProcessStatus.EARN, BigDecimal.valueOf(12450), null,
                LocalDateTime.of(2026, 5, 7, 14, 32)
        );

        CardPerformance performance = newCardPerformance(performanceId, BigDecimal.valueOf(820000), BigDecimal.valueOf(1.0));

        given(cardPointLedgerRepository.findById(pointLedgerId)).willReturn(Optional.of(pointLedger));

        given(cardPerformanceRepository.findByPerformanceIdAndCardUserUuid(performanceId, CARD_USER_UUID))
                .willReturn(Optional.of(performance));

        // when
        RewardLedgerDetailResponse response =
                rewardLedgerService.getRewardLedgerDetail(CARD_USER_UUID, pointLedgerId);

        // then
        assertThat(response.pointLedgerId()).isEqualTo(pointLedgerId);
        assertThat(response.baseMonth()).isEqualTo("2026-05");
        assertThat(response.type()).isEqualTo("EARN");
        assertThat(response.pointAmount()).isEqualTo(12450L);
        assertThat(response.occurredAt()).isEqualTo(LocalDateTime.of(2026, 5, 7, 14, 32));

        assertThat(response.detail().previousMonthSpendAmount()).isEqualTo(820000L);
        assertThat(response.detail().targetSpendAmount()).isEqualTo(500000L);
        assertThat(response.detail().shortfallAmount()).isEqualTo(0L);

    }

    @Test
    @DisplayName("미적용 리워드 내역 상세를 조회한다")
    void getRewardLedgerDetailNotApplied() throws Exception {
        // given
        Long pointLedgerId = 2L;
        Long performanceId = 30002L;

        CardPointLedger pointLedger = newCardPointLedger(pointLedgerId, performanceId,
                RewardProcessStatus.NOT_APPLIED, BigDecimal.ZERO, null,
                LocalDateTime.of(2026, 4, 7, 14, 32)
        );

        CardPerformance performance = newCardPerformance(performanceId, BigDecimal.valueOf(380000), BigDecimal.ZERO);

        given(cardPointLedgerRepository.findById(pointLedgerId)).willReturn(Optional.of(pointLedger));

        given(cardPerformanceRepository.findByPerformanceIdAndCardUserUuid(performanceId, CARD_USER_UUID))
                .willReturn(Optional.of(performance));

        // when
        RewardLedgerDetailResponse response =
                rewardLedgerService.getRewardLedgerDetail(CARD_USER_UUID, pointLedgerId);

        // then
        assertThat(response.pointLedgerId()).isEqualTo(pointLedgerId);
        assertThat(response.baseMonth()).isEqualTo("2026-04");
        assertThat(response.type()).isEqualTo("NOT_APPLIED");
        assertThat(response.pointAmount()).isEqualTo(0L);

        assertThat(response.detail().previousMonthSpendAmount()).isEqualTo(380000L);
        assertThat(response.detail().targetSpendAmount()).isEqualTo(500000L);
        assertThat(response.detail().shortfallAmount()).isEqualTo(120000L);
    }

    @Test
    @DisplayName("리워드 내역이 없으면 예외가 발생한다")
    void getRewardLedgerDetailNotFound() {
        // given
        Long pointLedgerId = 999999L;

        given(cardPointLedgerRepository.findById(pointLedgerId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> rewardLedgerService.getRewardLedgerDetail(CARD_USER_UUID, pointLedgerId))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode())
                            .isEqualTo(RewardErrorCode.REWARD_LEDGER_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("본인의 리워드 내역이 아니면 예외가 발생한다")
    void getRewardLedgerDetailForbidden() throws Exception {
        // given
        Long pointLedgerId = 1L;
        Long performanceId = 30001L;
        UUID otherCardUserUuid = UUID.fromString("99999999-9999-9999-9999-999999999999");

        CardPointLedger pointLedger = newCardPointLedger(
                pointLedgerId, performanceId, RewardProcessStatus.EARN,
                BigDecimal.valueOf(12450), null,
                LocalDateTime.of(2026, 5, 7, 14, 32)
        );

        given(cardPointLedgerRepository.findById(pointLedgerId)).willReturn(Optional.of(pointLedger));

        // when & then
        assertThatThrownBy(() -> rewardLedgerService.getRewardLedgerDetail(otherCardUserUuid, pointLedgerId))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode())
                            .isEqualTo(RewardErrorCode.REWARD_LEDGER_FORBIDDEN);
                });
    }

    @Test
    @DisplayName("리워드 산정 실적 정보가 없으면 예외가 발생한다")
    void getRewardLedgerDetailPerformanceNotFound() throws Exception {
        // given
        Long pointLedgerId = 1L;
        Long performanceId = 30001L;

        CardPointLedger pointLedger = newCardPointLedger(pointLedgerId, performanceId, RewardProcessStatus.EARN,
                BigDecimal.valueOf(12450), null,
                LocalDateTime.of(2026, 5, 7, 14, 32)
        );

        given(cardPointLedgerRepository.findById(pointLedgerId)).willReturn(Optional.of(pointLedger));

        given(cardPerformanceRepository.findByPerformanceIdAndCardUserUuid(performanceId, CARD_USER_UUID))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> rewardLedgerService.getRewardLedgerDetail(CARD_USER_UUID, pointLedgerId))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode())
                            .isEqualTo(CardPerformanceErrorCode.PERFORMANCE_NOT_FOUND);
                });
    }

    private CardPointLedger newCardPointLedger(Long pointLedgerId, Long performanceId, RewardProcessStatus rewardProcessStatus,
                                               BigDecimal inAmount, BigDecimal outAmount, LocalDateTime occurredAt) throws Exception {
        Constructor<CardPointLedger> constructor = CardPointLedger.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        CardPointLedger ledger = constructor.newInstance();

        setField(ledger, "pointLedgerId", pointLedgerId);
        setField(ledger, "pointUuid", POINT_UUID);
        setField(ledger, "cardUserUuid", CARD_USER_UUID);
        setField(ledger, "performanceId", performanceId);
        setField(ledger, "baseMonth", toBaseMonth(occurredAt));
        setField(ledger, "rewardProcessStatus", rewardProcessStatus);
        setField(ledger, "inAmount", inAmount);
        setField(ledger, "outAmount", outAmount);
        setField(ledger, "balanceAfterAmount", BigDecimal.valueOf(12450));
        setField(ledger, "occurredAt", occurredAt);

        return ledger;
    }

    private CardPerformance newCardPerformance(Long performanceId, BigDecimal previousMonthSpendAmount, BigDecimal rewardRate) throws Exception {
        Constructor<CardPerformance> constructor = CardPerformance.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        CardPerformance performance = constructor.newInstance();

        setField(performance, "performanceId", performanceId);
        setField(performance, "userUuid", USER_UUID);
        setField(performance, "cardUserUuid", CARD_USER_UUID);
        setField(performance, "baseMonth", "2026-05");
        setField(performance, "previousMonthSpendAmount", previousMonthSpendAmount);
        setField(performance, "currentMonthSpendAmount", BigDecimal.valueOf(1245000));
        setField(performance, "rewardRate", rewardRate);
        setField(performance, "rewardPointAmount", BigDecimal.valueOf(12450));
        setField(performance, "limitApplyStatus", "NONE");
        setField(performance, "performanceStatus", "CONFIRMED");
        setField(performance, "calculatedAt", LocalDateTime.of(2026, 5, 7, 14, 0));
        setField(performance, "confirmedAt", LocalDateTime.of(2026, 5, 7, 14, 10));

        return performance;
    }

    private CardPointLedger newCardPointLedger(
            Long pointLedgerId,
            RewardProcessStatus rewardProcessStatus,
            BigDecimal inAmount,
            BigDecimal outAmount,
            LocalDateTime occurredAt
    ) throws Exception {
        Constructor<CardPointLedger> constructor = CardPointLedger.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        CardPointLedger ledger = constructor.newInstance();
        setField(ledger, "pointLedgerId", pointLedgerId);
        setField(ledger, "pointUuid", POINT_UUID);
        setField(ledger, "cardUserUuid", CARD_USER_UUID);
        setField(ledger, "baseMonth", toBaseMonth(occurredAt));
        setField(ledger, "rewardProcessStatus", rewardProcessStatus);
        setField(ledger, "inAmount", inAmount);
        setField(ledger, "outAmount", outAmount);
        setField(ledger, "balanceAfterAmount", BigDecimal.valueOf(1245000));
        setField(ledger, "occurredAt", occurredAt);

        return ledger;
    }

    private String toBaseMonth(LocalDateTime occurredAt) {
        return occurredAt.getYear() + "-" + String.format("%02d", occurredAt.getMonthValue());
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
