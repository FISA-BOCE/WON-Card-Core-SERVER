package com.woorifisa.won_card_core_server.domain.reward.service;

import com.woorifisa.won_card_core_server.domain.performance.exception.code.CardPerformanceErrorCode;
import com.woorifisa.won_card_core_server.domain.performance.model.CardPerformance;
import com.woorifisa.won_card_core_server.domain.performance.repository.CardPerformanceRepository;
import com.woorifisa.won_card_core_server.domain.reward.dto.request.RewardSweepResultRequest;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepCancelResponse;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepCandidateResponse;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepRequestResponse;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepResultResponse;
import com.woorifisa.won_card_core_server.domain.reward.exception.code.RewardErrorCode;
import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;
import com.woorifisa.won_card_core_server.domain.reward.model.RewardSweepBatchExecution;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.RewardProcessStatus;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.SweepStatus;
import com.woorifisa.won_card_core_server.domain.reward.repository.CardPointLedgerRepository;
import com.woorifisa.won_card_core_server.domain.reward.repository.RewardSweepBatchExecutionRepository;
import com.woorifisa.won_card_core_server.domain.reward.service.validator.RewardLedgerValidator;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RewardSweepServiceTest {

    private CardPointLedgerRepository cardPointLedgerRepository;
    private CardPerformanceRepository cardPerformanceRepository;
    private RewardSweepBatchExecutionRepository rewardSweepBatchExecutionRepository;
    private RewardLedgerValidator rewardLedgerValidator;
    private RewardSweepService rewardSweepService;

    private final UUID cardUserUuid = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setUp() {
        cardPointLedgerRepository = mock(CardPointLedgerRepository.class);
        cardPerformanceRepository = mock(CardPerformanceRepository.class);
        rewardSweepBatchExecutionRepository = mock(RewardSweepBatchExecutionRepository.class);
        rewardLedgerValidator = new RewardLedgerValidator();

        rewardSweepService = new RewardSweepService(
                cardPointLedgerRepository,
                cardPerformanceRepository,
                rewardLedgerValidator,
                rewardSweepBatchExecutionRepository
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

    @Test
    @DisplayName("baseMonth 기준으로 스윕 후보 원장 목록을 반환한다")
    void getSweepCandidatesSuccess() {
        // given
        CardPointLedger ledger = createLedger(1L,
                cardUserUuid,
                10L,
                RewardProcessStatus.EARN,
                SweepStatus.NONE,
                BigDecimal.valueOf(12450),
                BigDecimal.ZERO
        );

        when(cardPointLedgerRepository.findSweepCandidates("2026-05", RewardProcessStatus.EARN, SweepStatus.NONE)).thenReturn(List.of(ledger));

        // when
        RewardSweepCandidateResponse response = rewardSweepService.getSweepCandidates("2026-05");

        // then
        assertThat(response.baseMonth()).isEqualTo("2026-05");
        assertThat(response.candidates()).hasSize(1);

        RewardSweepCandidateResponse.RewardSweepCandidateItem candidate = response.candidates().get(0);

        assertThat(candidate.pointLedgerId()).isEqualTo(1L);
        assertThat(candidate.cardUserUuid()).isEqualTo(cardUserUuid);
        assertThat(candidate.performanceId()).isEqualTo(10L);
        assertThat(candidate.baseMonth()).isEqualTo("2026-05");
        assertThat(candidate.pointAmount()).isEqualTo(12450L);
        assertThat(candidate.krwAmount()).isEqualTo(12450L);

        verify(cardPointLedgerRepository).findSweepCandidates(
                "2026-05",
                RewardProcessStatus.EARN,
                SweepStatus.NONE
        );
    }

    @Test
    @DisplayName("스윕 후보 원장이 없으면 빈 목록을 반환한다")
    void getSweepCandidatesEmpty() {
        // given
        when(cardPointLedgerRepository.findSweepCandidates("2026-05", RewardProcessStatus.EARN, SweepStatus.NONE)).thenReturn(List.of());

        // when
        RewardSweepCandidateResponse response = rewardSweepService.getSweepCandidates("2026-05");

        // then
        assertThat(response.baseMonth()).isEqualTo("2026-05");
        assertThat(response.candidates()).isEmpty();

        verify(cardPointLedgerRepository).findSweepCandidates("2026-05", RewardProcessStatus.EARN, SweepStatus.NONE);
    }

    @Test
    @DisplayName("baseMonth 형식이 올바르지 않으면 예외가 발생한다")
    void getSweepCandidatesInvalidBaseMonth() {
        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> rewardSweepService.getSweepCandidates("202605"));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(RewardErrorCode.INVALID_REWARD_BASE_MONTH);

        verify(cardPointLedgerRepository, never()).findSweepCandidates(any(), any(), any());
    }

    @Test
    @DisplayName("조회된 원장의 스윕 가능 금액이 없으면 후보에서 제외한다")
    void getSweepCandidatesSkipInvalidAmount() {
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

        when(cardPointLedgerRepository.findSweepCandidates("2026-05", RewardProcessStatus.EARN, SweepStatus.NONE)).thenReturn(List.of(ledger));

        BusinessException exception = assertThrows(BusinessException.class, () -> rewardSweepService.getSweepCandidates("2026-05"));

        assertThat(exception.getErrorCode())
                .isEqualTo(RewardErrorCode.REWARD_SWEEP_AMOUNT_INVALID);
    }

    @Test
    @DisplayName("보상 트랜잭션 테스트: REQUESTED 상태 원장이면 스윕 요청을 취소하고 NONE 상태를 반환한다")
    void cancelSweepRequestSuccess() {
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
        setField(ledger, "sweepRequestId", 100L);

        when(cardPointLedgerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ledger));

        // when
        RewardSweepCancelResponse response =
                rewardSweepService.cancelSweepRequest(cardUserUuid, 1L);

        // then
        assertThat(ledger.getSweepStatus()).isEqualTo(SweepStatus.NONE);
        assertThat(ledger.getSweepRequestId()).isNull();
        assertThat(response.pointLedgerId()).isEqualTo(1L);
        assertThat(response.sweepStatus()).isEqualTo(SweepStatus.NONE.name());

        verify(cardPointLedgerRepository).findByIdForUpdate(1L);
    }

    @Test
    @DisplayName("이미 NONE 상태인 원장은 취소 요청을 멱등하게 성공 처리한다")
    void cancelSweepRequestAlreadyNone() {
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

        // when
        RewardSweepCancelResponse response =
                rewardSweepService.cancelSweepRequest(cardUserUuid, 1L);

        // then
        assertThat(ledger.getSweepStatus()).isEqualTo(SweepStatus.NONE);
        assertThat(response.sweepStatus()).isEqualTo(SweepStatus.NONE.name());
    }

    @Test
    @DisplayName("COMPLETED 상태 원장은 스윕 요청을 취소할 수 없다")
    void cancelSweepRequestCompletedNotAllowed() {
        // given
        CardPointLedger ledger = createLedger(
                1L,
                cardUserUuid,
                10L,
                RewardProcessStatus.EARN,
                SweepStatus.COMPLETED,
                BigDecimal.valueOf(12450),
                BigDecimal.ZERO
        );

        when(cardPointLedgerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ledger));

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rewardSweepService.cancelSweepRequest(cardUserUuid, 1L)
        );

        // then
        assertThat(exception.getErrorCode())
                .isEqualTo(RewardErrorCode.REWARD_SWEEP_CANCEL_NOT_ALLOWED);
        assertThat(ledger.getSweepStatus()).isEqualTo(SweepStatus.COMPLETED);
    }

    @Test
    @DisplayName("FAILED 상태 원장은 스윕 요청을 취소할 수 없다")
    void cancelSweepRequestFailedNotAllowed() {
        // given
        CardPointLedger ledger = createLedger(
                1L,
                cardUserUuid,
                10L,
                RewardProcessStatus.EARN,
                SweepStatus.FAILED,
                BigDecimal.valueOf(12450),
                BigDecimal.ZERO
        );

        when(cardPointLedgerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ledger));

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rewardSweepService.cancelSweepRequest(cardUserUuid, 1L)
        );

        // then
        assertThat(exception.getErrorCode())
                .isEqualTo(RewardErrorCode.REWARD_SWEEP_CANCEL_NOT_ALLOWED);
        assertThat(ledger.getSweepStatus()).isEqualTo(SweepStatus.FAILED);
    }

    @Test
    @DisplayName("취소 요청에서 cardUserUuid가 null이면 예외가 발생한다")
    void cancelSweepRequestCardUserUuidNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rewardSweepService.cancelSweepRequest(null, 1L)
        );

        assertThat(exception.getErrorCode()).isEqualTo(RewardErrorCode.INVALID_REWARD_LEDGER_TYPE);
        verify(cardPointLedgerRepository, never()).findByIdForUpdate(any());
    }

    @Test
    @DisplayName("취소 요청에서 원장이 없으면 REWARD_LEDGER_NOT_FOUND 예외가 발생한다")
    void cancelSweepRequestLedgerNotFound() {
        when(cardPointLedgerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rewardSweepService.cancelSweepRequest(cardUserUuid, 1L)
        );

        assertThat(exception.getErrorCode()).isEqualTo(RewardErrorCode.REWARD_LEDGER_NOT_FOUND);
    }

    @Test
    @DisplayName("취소 요청에서 다른 카드 사용자의 원장이면 REWARD_LEDGER_FORBIDDEN 예외가 발생한다")
    void cancelSweepRequestForbidden() {
        UUID otherCardUserUuid = UUID.fromString("99999999-9999-9999-9999-999999999999");

        CardPointLedger ledger = createLedger(
                1L,
                otherCardUserUuid,
                10L,
                RewardProcessStatus.EARN,
                SweepStatus.REQUESTED,
                BigDecimal.valueOf(12450),
                BigDecimal.ZERO
        );

        when(cardPointLedgerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ledger));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rewardSweepService.cancelSweepRequest(cardUserUuid, 1L)
        );

        assertThat(exception.getErrorCode()).isEqualTo(RewardErrorCode.REWARD_LEDGER_FORBIDDEN);
        assertThat(ledger.getSweepStatus()).isEqualTo(SweepStatus.REQUESTED);
    }

    @Test
    @DisplayName("투자 완료 결과를 받으면 REQUESTED 원장을 COMPLETED로 반영한다")
    void applySweepResultCompleted() {
        CardPointLedger ledger = createLedger(
                1L,
                cardUserUuid,
                10L,
                RewardProcessStatus.EARN,
                SweepStatus.REQUESTED,
                BigDecimal.valueOf(1000),
                BigDecimal.ZERO
        );
        setField(ledger, "batchExecutionId", 10L);
        RewardSweepBatchExecution batch = RewardSweepBatchExecution.start("2026-05", LocalDateTime.now());
        setField(batch, "batchExecutionId", 10L);
        setField(batch, "totalCandidateCount", 1L);

        when(cardPointLedgerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ledger));
        when(rewardSweepBatchExecutionRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(batch));

        RewardSweepResultRequest request = new RewardSweepResultRequest(
                2L,
                1L,
                "CORR-SWEEP-TEST-1",
                "SWEEP:POINT_LEDGER:1",
                SweepStatus.COMPLETED,
                null,
                null
        );

        RewardSweepResultResponse response =
                rewardSweepService.applySweepResult(cardUserUuid, 1L, request);

        assertThat(ledger.getSweepStatus()).isEqualTo(SweepStatus.COMPLETED);
        assertThat(response.pointLedgerId()).isEqualTo(1L);
        assertThat(response.sweepStatus()).isEqualTo(SweepStatus.COMPLETED.name());
        assertThat(batch.getCompletedCount()).isEqualTo(1);
        verify(rewardSweepBatchExecutionRepository).findByIdForUpdate(10L);
    }

    @Test
    @DisplayName("투자 실패 결과를 받으면 REQUESTED 원장을 FAILED로 반영한다")
    void applySweepResultFailed() {
        CardPointLedger ledger = createLedger(
                1L,
                cardUserUuid,
                10L,
                RewardProcessStatus.EARN,
                SweepStatus.REQUESTED,
                BigDecimal.valueOf(1000),
                BigDecimal.ZERO
        );
        setField(ledger, "batchExecutionId", 10L);
        RewardSweepBatchExecution batch = RewardSweepBatchExecution.start("2026-05", LocalDateTime.now());
        setField(batch, "batchExecutionId", 10L);
        setField(batch, "totalCandidateCount", 1L);

        when(cardPointLedgerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ledger));
        when(rewardSweepBatchExecutionRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(batch));

        RewardSweepResultRequest request = new RewardSweepResultRequest(
                2L,
                1L,
                "CORR-SWEEP-TEST-1",
                "SWEEP:POINT_LEDGER:1",
                SweepStatus.FAILED,
                "SWEEP_FAIL_006",
                "ETF 가격을 조회할 수 없습니다."
        );

        RewardSweepResultResponse response =
                rewardSweepService.applySweepResult(cardUserUuid, 1L, request);

        assertThat(ledger.getSweepStatus()).isEqualTo(SweepStatus.FAILED);
        assertThat(response.sweepStatus()).isEqualTo(SweepStatus.FAILED.name());
        assertThat(batch.getFailedCount()).isEqualTo(1);
        verify(rewardSweepBatchExecutionRepository).findByIdForUpdate(10L);
    }

    @Test
    @DisplayName("이미 COMPLETED인 원장에 COMPLETED 결과가 다시 들어오면 멱등 성공한다")
    void applySweepResultCompletedIdempotent() {
        CardPointLedger ledger = createLedger(
                1L,
                cardUserUuid,
                10L,
                RewardProcessStatus.EARN,
                SweepStatus.COMPLETED,
                BigDecimal.valueOf(1000),
                BigDecimal.ZERO
        );

        when(cardPointLedgerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ledger));

        RewardSweepResultRequest request = new RewardSweepResultRequest(
                2L,
                1L,
                "CORR-SWEEP-TEST-1",
                "SWEEP:POINT_LEDGER:1",
                SweepStatus.COMPLETED,
                null,
                null
        );

        RewardSweepResultResponse response =
                rewardSweepService.applySweepResult(cardUserUuid, 1L, request);

        assertThat(ledger.getSweepStatus()).isEqualTo(SweepStatus.COMPLETED);
        assertThat(response.sweepStatus()).isEqualTo(SweepStatus.COMPLETED.name());
    }

    @Test
    @DisplayName("REQUESTED 상태가 아닌 원장에는 결과를 반영할 수 없다")
    void applySweepResultNotRequested() {
        CardPointLedger ledger = createLedger(
                1L,
                cardUserUuid,
                10L,
                RewardProcessStatus.EARN,
                SweepStatus.NONE,
                BigDecimal.valueOf(1000),
                BigDecimal.ZERO
        );

        when(cardPointLedgerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ledger));

        RewardSweepResultRequest request = new RewardSweepResultRequest(
                2L,
                1L,
                "CORR-SWEEP-TEST-1",
                "SWEEP:POINT_LEDGER:1",
                SweepStatus.COMPLETED,
                null,
                null
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rewardSweepService.applySweepResult(cardUserUuid, 1L, request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(RewardErrorCode.REWARD_SWEEP_NOT_ELIGIBLE);
    }

    @Test
    @DisplayName("결과 상태가 COMPLETED 또는 FAILED가 아니면 예외를 던진다")
    void applySweepResultInvalidResultStatus() {
        CardPointLedger ledger = createLedger(
                1L,
                cardUserUuid,
                10L,
                RewardProcessStatus.EARN,
                SweepStatus.REQUESTED,
                BigDecimal.valueOf(1000),
                BigDecimal.ZERO
        );

        when(cardPointLedgerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(ledger));

        RewardSweepResultRequest request = new RewardSweepResultRequest(
                2L,
                1L,
                "CORR-SWEEP-TEST-1",
                "SWEEP:POINT_LEDGER:1",
                SweepStatus.REQUESTED,
                null,
                null
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rewardSweepService.applySweepResult(cardUserUuid, 1L, request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(RewardErrorCode.INVALID_REWARD_LEDGER_STATUS);
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
