package com.woorifisa.won_card_core_server.domain.reward.service;

import com.woorifisa.won_card_core_server.domain.reward.dto.result.RewardSweepChunkReservationResult;
import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;
import com.woorifisa.won_card_core_server.domain.reward.model.RewardSweepBatchExecution;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.RewardProcessStatus;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.SweepStatus;
import com.woorifisa.won_card_core_server.domain.reward.repository.CardPointLedgerRepository;
import com.woorifisa.won_card_core_server.domain.reward.repository.RewardSweepBatchExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RewardSweepBatchChunkReservationServiceTest {

    private RewardSweepBatchExecutionRepository batchRepository;
    private CardPointLedgerRepository pointLedgerRepository;
    private RewardSweepBatchChunkReservationService service;

    @BeforeEach
    void setUp() {
        batchRepository = mock(RewardSweepBatchExecutionRepository.class);
        pointLedgerRepository = mock(CardPointLedgerRepository.class);

        service = new RewardSweepBatchChunkReservationService(
                batchRepository,
                pointLedgerRepository
        );
    }

    @Test
    @DisplayName("후보 chunk를 REQUESTED로 선점하고 선점 항목을 반환한다")
    void reserveCandidatesReturnsReservedItems() {
        // given
        RewardSweepBatchExecution batch = RewardSweepBatchExecution.start("2026-06", LocalDateTime.now());
        setField(batch, "batchExecutionId", 10L);

        CardPointLedger first = createLedger(1L, "2026-06");
        CardPointLedger second = createLedger(2L, "2026-06");

        when(batchRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(batch));
        when(pointLedgerRepository.findSweepCandidateChunk(
                eq("2026-06"),
                eq(RewardProcessStatus.EARN),
                eq(SweepStatus.NONE),
                eq(0L),
                any(PageRequest.class)
        )).thenReturn(List.of(first, second));

        // when
        RewardSweepChunkReservationResult result = service.reserve(10L, 300);

        // then
        assertThat(result.reservedCount()).isEqualTo(2);
        assertThat(result.lastProcessedPointLedgerId()).isEqualTo(2L);
        assertThat(result.reservedItems()).hasSize(2);

        assertThat(first.getSweepStatus()).isEqualTo(SweepStatus.REQUESTED);
        assertThat(first.getBatchExecutionId()).isEqualTo(10L);
        assertThat(first.getSweepRequestId()).isEqualTo(1L);
        assertThat(first.getIdempotencyKey()).isEqualTo("CARD_SWEEP:1:2026-06");
        assertThat(first.getSweepRequestedAt()).isNotNull();

        assertThat(second.getSweepStatus()).isEqualTo(SweepStatus.REQUESTED);
        assertThat(second.getIdempotencyKey()).isEqualTo("CARD_SWEEP:2:2026-06");

        var firstItem = result.reservedItems().get(0);
        assertThat(firstItem.sweepRequestId()).isEqualTo(1L);
        assertThat(firstItem.eventType()).isEqualTo("SWEEP_REQUESTED");
        assertThat(firstItem.eventId()).isEqualTo("CARD-SWEEP-1");
        assertThat(firstItem.correlationId()).isEqualTo("CARD-SWEEP-1");
        assertThat(firstItem.idempotencyKey()).isEqualTo("CARD_SWEEP:1:2026-06");
        assertThat(firstItem.cardUserUuid()).isEqualTo(first.getCardUserUuid());
        assertThat(firstItem.performanceId()).isEqualTo(100L);
        assertThat(firstItem.pointLedgerId()).isEqualTo(1L);
        assertThat(firstItem.baseMonth()).isEqualTo("2026-06");
        assertThat(firstItem.pointAmount()).isEqualTo(10000L);
        assertThat(firstItem.krwAmount()).isEqualTo(10000L);
        assertThat(firstItem.requestedAt()).isEqualTo(first.getSweepRequestedAt());

        assertThat(batch.getRequestedCount()).isEqualTo(2);
        assertThat(batch.getTotalCandidateCount()).isEqualTo(2);
        assertThat(batch.getLastProcessedPointLedgerId()).isEqualTo(2L);

        verify(batchRepository).findByIdForUpdate(10L);
    }

    @Test
    @DisplayName("후보가 없으면 batch 카운터를 변경하지 않고 빈 선점 항목을 반환한다")
    void reserveEmptyCandidatesDoesNothing() {
        // given
        RewardSweepBatchExecution batch = RewardSweepBatchExecution.start("2026-06", LocalDateTime.now());
        setField(batch, "batchExecutionId", 10L);

        when(batchRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(batch));
        when(pointLedgerRepository.findSweepCandidateChunk(
                eq("2026-06"),
                eq(RewardProcessStatus.EARN),
                eq(SweepStatus.NONE),
                eq(20L),
                any(PageRequest.class)
        )).thenReturn(List.of());

        // when
        setField(batch, "lastProcessedPointLedgerId", 20L);
        RewardSweepChunkReservationResult result = service.reserve(10L, 300);

        // then
        assertThat(result.reservedCount()).isZero();
        assertThat(result.lastProcessedPointLedgerId()).isEqualTo(20L);
        assertThat(result.reservedItems()).isEmpty();
        assertThat(batch.getRequestedCount()).isZero();
    }

    private CardPointLedger createLedger(Long pointLedgerId, String baseMonth) {
        try {
            Constructor<CardPointLedger> constructor = CardPointLedger.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            CardPointLedger ledger = constructor.newInstance();

            setField(ledger, "pointLedgerId", pointLedgerId);
            setField(ledger, "pointUuid", UUID.randomUUID());
            setField(ledger, "cardUserUuid", UUID.fromString("22222222-2222-2222-2222-222222222222"));
            setField(ledger, "performanceId", 100L);
            setField(ledger, "baseMonth", baseMonth);
            setField(ledger, "rewardProcessStatus", RewardProcessStatus.EARN);
            setField(ledger, "sweepStatus", SweepStatus.NONE);
            setField(ledger, "inAmount", BigDecimal.valueOf(10000));
            setField(ledger, "outAmount", BigDecimal.ZERO);
            return ledger;
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
