package com.woorifisa.won_card_core_server.domain.reward.service;

import com.woorifisa.won_card_core_server.domain.performance.exception.code.CardPerformanceErrorCode;
import com.woorifisa.won_card_core_server.domain.performance.model.CardPerformance;
import com.woorifisa.won_card_core_server.domain.performance.repository.CardPerformanceRepository;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepRequestResponse;
import com.woorifisa.won_card_core_server.domain.reward.exception.code.RewardErrorCode;
import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.RewardProcessStatus;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.SweepStatus;
import com.woorifisa.won_card_core_server.domain.reward.repository.CardPointLedgerRepository;
import com.woorifisa.won_card_core_server.domain.reward.service.validator.RewardLedgerValidator;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RewardSweepServiceTest {

    private CardPointLedgerRepository cardPointLedgerRepository;
    private CardPerformanceRepository cardPerformanceRepository;
    private RewardLedgerValidator rewardLedgerValidator;
    private RewardSweepService rewardSweepService;

    private final UUID cardUserUuid = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setUp() {
        cardPointLedgerRepository = mock(CardPointLedgerRepository.class);
        cardPerformanceRepository = mock(CardPerformanceRepository.class);
        rewardLedgerValidator = new RewardLedgerValidator();

        rewardSweepService = new RewardSweepService(
                cardPointLedgerRepository,
                cardPerformanceRepository,
                rewardLedgerValidator
        );
    }

    @Test
    @DisplayName("스윕 가능한 원장이면 sweepStatus를 REQUESTED로 변경하고 응답을 반환한다")
    void requestSweepSuccess() {
        // given
        CardPointLedger ledger = createLedger(
                1L,
                cardUserUuid,
                10L,
                RewardProcessStatus.EARN,
                SweepStatus.NONE,
                BigDecimal.valueOf(12450),
                BigDecimal.ZERO
        );

        CardPerformance performance = createPerformance(
                10L,
                cardUserUuid,
                "2026-05"
        );

        when(cardPointLedgerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ledger));
        when(cardPerformanceRepository.findByPerformanceIdAndCardUserUuid(10L, cardUserUuid))
                .thenReturn(Optional.of(performance));

        // when
        RewardSweepRequestResponse response = rewardSweepService.requestSweep(cardUserUuid, 1L);

        // then
        assertThat(ledger.getSweepStatus()).isEqualTo(SweepStatus.REQUESTED);

        assertThat(response.pointLedgerId()).isEqualTo(1L);
        assertThat(response.performanceId()).isEqualTo(10L);
        assertThat(response.baseMonth()).isEqualTo("2026-05");
        assertThat(response.pointAmount()).isEqualTo(12450L);
        assertThat(response.krwAmount()).isEqualTo(12450L);
        assertThat(response.sweepStatus()).isEqualTo(SweepStatus.REQUESTED.name());

        verify(cardPointLedgerRepository).findByIdForUpdate(1L);
        verify(cardPerformanceRepository).findByPerformanceIdAndCardUserUuid(10L, cardUserUuid);
    }

    @Test
    @DisplayName("cardUserUuid가 null이면 예외가 발생한다")
    void requestSweepCardUserUuidNull() {
        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rewardSweepService.requestSweep(null, 1L)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(RewardErrorCode.INVALID_REWARD_LEDGER_TYPE);
        verify(cardPointLedgerRepository, never()).findByIdForUpdate(any());
        verify(cardPerformanceRepository, never()).findByPerformanceIdAndCardUserUuid(any(), any());
    }

    @Test
    @DisplayName("pointLedgerId가 null이면 예외가 발생한다")
    void requestSweepPointLedgerIdNull() {
        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rewardSweepService.requestSweep(cardUserUuid, null)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(RewardErrorCode.INVALID_REWARD_LEDGER_TYPE);
        verify(cardPointLedgerRepository, never()).findByIdForUpdate(any());
        verify(cardPerformanceRepository, never()).findByPerformanceIdAndCardUserUuid(any(), any());
    }

    @Test
    @DisplayName("원장이 없으면 REWARD_LEDGER_NOT_FOUND 예외가 발생한다")
    void requestSweepLedgerNotFound() {
        // given
        when(cardPointLedgerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.empty());

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rewardSweepService.requestSweep(cardUserUuid, 1L)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(RewardErrorCode.REWARD_LEDGER_NOT_FOUND);
        verify(cardPerformanceRepository, never()).findByPerformanceIdAndCardUserUuid(any(), any());
    }

    @Test
    @DisplayName("다른 카드 사용자의 원장이면 REWARD_LEDGER_FORBIDDEN 예외가 발생한다")
    void requestSweepForbidden() {
        // given
        UUID otherCardUserUuid = UUID.fromString("99999999-9999-9999-9999-999999999999");

        CardPointLedger ledger = createLedger(
                1L,
                otherCardUserUuid,
                10L,
                RewardProcessStatus.EARN,
                SweepStatus.NONE,
                BigDecimal.valueOf(12450),
                BigDecimal.ZERO
        );

        when(cardPointLedgerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ledger));

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rewardSweepService.requestSweep(cardUserUuid, 1L)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(RewardErrorCode.REWARD_LEDGER_FORBIDDEN);
        assertThat(ledger.getSweepStatus()).isEqualTo(SweepStatus.NONE);
        verify(cardPerformanceRepository, never()).findByPerformanceIdAndCardUserUuid(any(), any());
    }

    @Test
    @DisplayName("EARN 상태가 아니면 스윕할 수 없다")
    void requestSweepNotEarn() {
        // given
        CardPointLedger ledger = createLedger(
                1L,
                cardUserUuid,
                10L,
                RewardProcessStatus.NOT_APPLIED,
                SweepStatus.NONE,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        when(cardPointLedgerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ledger));

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rewardSweepService.requestSweep(cardUserUuid, 1L)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(RewardErrorCode.REWARD_SWEEP_NOT_ELIGIBLE);
        assertThat(ledger.getSweepStatus()).isEqualTo(SweepStatus.NONE);
        verify(cardPerformanceRepository, never()).findByPerformanceIdAndCardUserUuid(any(), any());
    }

    @Test
    @DisplayName("이미 스윕 요청된 원장이면 예외가 발생한다")
    void requestSweepAlreadyRequested() {
        // given
        CardPointLedger ledger = createLedger(
                1L,
                cardUserUuid,
                10L,
                RewardProcessStatus.EARN,
                SweepStatus.REQUESTED,
                BigDecimal.valueOf(12450),
                BigDecimal.ZERO
        );

        when(cardPointLedgerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ledger));

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rewardSweepService.requestSweep(cardUserUuid, 1L)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(RewardErrorCode.REWARD_SWEEP_ALREADY_REQUESTED);
        assertThat(ledger.getSweepStatus()).isEqualTo(SweepStatus.REQUESTED);
        verify(cardPerformanceRepository, never()).findByPerformanceIdAndCardUserUuid(any(), any());
    }

    @Test
    @DisplayName("스윕 가능 금액이 0원이면 예외가 발생한다")
    void requestSweepInvalidAmount() {
        // given
        CardPointLedger ledger = createLedger(
                1L,
                cardUserUuid,
                10L,
                RewardProcessStatus.EARN,
                SweepStatus.NONE,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        when(cardPointLedgerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ledger));

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rewardSweepService.requestSweep(cardUserUuid, 1L)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(RewardErrorCode.REWARD_SWEEP_AMOUNT_INVALID);
        assertThat(ledger.getSweepStatus()).isEqualTo(SweepStatus.NONE);
        verify(cardPerformanceRepository, never()).findByPerformanceIdAndCardUserUuid(any(), any());
    }

    @Test
    @DisplayName("실적 정보가 없으면 예외가 발생한다")
    void requestSweepPerformanceNotFound() {
        // given
        CardPointLedger ledger = createLedger(
                1L,
                cardUserUuid,
                10L,
                RewardProcessStatus.EARN,
                SweepStatus.NONE,
                BigDecimal.valueOf(12450),
                BigDecimal.ZERO
        );

        when(cardPointLedgerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ledger));
        when(cardPerformanceRepository.findByPerformanceIdAndCardUserUuid(10L, cardUserUuid))
                .thenReturn(Optional.empty());

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rewardSweepService.requestSweep(cardUserUuid, 1L)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(CardPerformanceErrorCode.PERFORMANCE_NOT_FOUND);
        assertThat(ledger.getSweepStatus()).isEqualTo(SweepStatus.NONE);
    }

    private CardPointLedger createLedger(
            Long pointLedgerId,
            UUID cardUserUuid,
            Long performanceId,
            RewardProcessStatus rewardProcessStatus,
            SweepStatus sweepStatus,
            BigDecimal inAmount,
            BigDecimal outAmount
    ) {
        try {
            Constructor<CardPointLedger> constructor = CardPointLedger.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            CardPointLedger ledger = constructor.newInstance();

            setField(ledger, "pointLedgerId", pointLedgerId);
            setField(ledger, "cardUserUuid", cardUserUuid);
            setField(ledger, "performanceId", performanceId);
            setField(ledger, "baseMonth", "2026-05");
            setField(ledger, "rewardProcessStatus", rewardProcessStatus);
            setField(ledger, "sweepStatus", sweepStatus);
            setField(ledger, "inAmount", inAmount);
            setField(ledger, "outAmount", outAmount);

            return ledger;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CardPerformance createPerformance(
            Long performanceId,
            UUID cardUserUuid,
            String baseMonth
    ) {
        try {
            Constructor<CardPerformance> constructor = CardPerformance.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            CardPerformance performance = constructor.newInstance();

            setField(performance, "performanceId", performanceId);
            setField(performance, "cardUserUuid", cardUserUuid);
            setField(performance, "baseMonth", baseMonth);

            return performance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
