package com.woorifisa.won_card_core_server.domain.reward.service;

import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepReservedItemResponse;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardSweepReservationResponse;
import com.woorifisa.won_card_core_server.domain.reward.dto.result.RewardSweepChunkReservationResult;
import com.woorifisa.won_card_core_server.domain.reward.exception.code.RewardErrorCode;
import com.woorifisa.won_card_core_server.domain.reward.model.RewardSweepBatchExecution;
import com.woorifisa.won_card_core_server.domain.reward.repository.RewardSweepBatchExecutionRepository;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RewardSweepBatchServiceTest {

    private RewardSweepBatchExecutionRepository batchRepository;
    private RewardSweepBatchChunkReservationService reservationService;
    private RewardSweepBatchService service;

    @BeforeEach
    void setUp() {
        batchRepository = mock(RewardSweepBatchExecutionRepository.class);
        reservationService = mock(RewardSweepBatchChunkReservationService.class);
        service = new RewardSweepBatchService(batchRepository, reservationService);
    }

    @Test
    @DisplayName("배치 시작 시 실행 이력만 생성하고 선점은 수행하지 않는다")
    void startCreatesBatchOnly() {
        // given
        when(batchRepository.existsByBaseMonthAndStatusIn(eq("2026-06"), any()))
                .thenReturn(false);
        when(batchRepository.save(any(RewardSweepBatchExecution.class)))
                .thenAnswer(invocation -> {
                    RewardSweepBatchExecution batch = invocation.getArgument(0);
                    setField(batch, "batchExecutionId", 10L);
                    return batch;
                });

        // when
        var response = service.start("2026-06", 300);

        // then
        assertThat(response.batchExecutionId()).isEqualTo(10L);
        assertThat(response.baseMonth()).isEqualTo("2026-06");
        assertThat(response.status()).isEqualTo("RUNNING");
        assertThat(response.requestedCount()).isZero();
    }

    @Test
    @DisplayName("배치 선점 요청 시 선점 결과를 응답으로 변환한다")
    void reserveReturnsReservedItems() {
        // given
        RewardSweepBatchExecution batch = RewardSweepBatchExecution.start("2026-06", LocalDateTime.now());
        setField(batch, "batchExecutionId", 10L);

        RewardSweepReservedItemResponse item = new RewardSweepReservedItemResponse(
                1L,
                "SWEEP_REQUESTED",
                "CARD-SWEEP-1",
                "CARD-SWEEP-1",
                "CARD_SWEEP:1:2026-06",
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                100L,
                1L,
                "2026-06",
                10000L,
                10000L,
                LocalDateTime.of(2026, 6, 16, 0, 30)
        );

        when(reservationService.reserve(10L, 300))
                .thenReturn(new RewardSweepChunkReservationResult(1, 1L, List.of(item)));
        when(batchRepository.findById(10L)).thenReturn(Optional.of(batch));

        // when
        RewardSweepReservationResponse response = service.reserve(10L, 300);

        // then
        assertThat(response.batchExecutionId()).isEqualTo(10L);
        assertThat(response.baseMonth()).isEqualTo("2026-06");
        assertThat(response.status()).isEqualTo("RUNNING");
        assertThat(response.reservedCount()).isEqualTo(1);
        assertThat(response.lastProcessedPointLedgerId()).isEqualTo(1L);
        assertThat(response.reservedItems()).containsExactly(item);
    }

    @Test
    @DisplayName("실행 중인 배치가 있으면 비즈니스 예외를 던진다")
    void startThrowsBusinessExceptionWhenBatchAlreadyRunning() {
        // given
        when(batchRepository.existsByBaseMonthAndStatusIn(eq("2026-06"), any()))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> service.start("2026-06", 300))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(RewardErrorCode.REWARD_SWEEP_BATCH_ALREADY_RUNNING);
    }

    @Test
    @DisplayName("기준월 형식이 올바르지 않으면 비즈니스 예외를 던진다")
    void startThrowsBusinessExceptionWhenBaseMonthInvalid() {
        assertThatThrownBy(() -> service.start("2026-13", 300))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(RewardErrorCode.INVALID_REWARD_BASE_MONTH);
    }

    @Test
    @DisplayName("선점 chunkSize가 0 이하이면 비즈니스 예외를 던진다")
    void reserveThrowsBusinessExceptionWhenChunkSizeInvalid() {
        assertThatThrownBy(() -> service.reserve(10L, 0))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(RewardErrorCode.INVALID_REWARD_SWEEP_BATCH_SIZE);
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
