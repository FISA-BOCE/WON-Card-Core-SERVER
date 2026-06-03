package com.woorifisa.won_card_core_server.domain.reward.service;

import com.woorifisa.won_card_core_server.domain.reward.dto.response.SweepRequestOutboxResponse;
import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;
import com.woorifisa.won_card_core_server.domain.reward.model.SweepRequestOutbox;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.SweepRequestOutboxStatus;
import com.woorifisa.won_card_core_server.domain.reward.repository.SweepRequestOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SweepRequestOutboxServiceTest {

    private SweepRequestOutboxRepository outboxRepository;
    private SweepRequestOutboxService service;

    @BeforeEach
    void setUp() {
        outboxRepository = mock(SweepRequestOutboxRepository.class);
        service = new SweepRequestOutboxService(outboxRepository);
    }

    @Test
    @DisplayName("READY outbox를 claim하면 PUBLISHING으로 변경하고 응답을 반환한다")
    void claimReadyMarksPublishing() {
        // given
        SweepRequestOutbox outbox = createOutbox(1L);

        when(outboxRepository.findByStatusOrderByOutboxIdAsc(
                eq(SweepRequestOutboxStatus.READY),
                any(Pageable.class)
        )).thenReturn(List.of(outbox));

        // when
        List<SweepRequestOutboxResponse> responses = service.claimReady(100);

        // then
        assertThat(outbox.getStatus()).isEqualTo(SweepRequestOutboxStatus.PUBLISHING);
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).outboxId()).isEqualTo(1L);
        assertThat(responses.get(0).idempotencyKey()).isEqualTo("CARD_SWEEP:1:2026-06");
        assertThat(responses.get(0).payload()).isEqualTo("{\"eventType\":\"SWEEP_REQUESTED\"}");
    }

    @Test
    @DisplayName("발행 성공 처리하면 PUBLISHED 상태와 publishedAt을 저장한다")
    void markPublished() {
        // given
        SweepRequestOutbox outbox = createOutbox(1L);
        outbox.markPublishing();
        when(outboxRepository.findById(1L)).thenReturn(Optional.of(outbox));

        // when
        service.markPublished(1L);

        // then
        assertThat(outbox.getStatus()).isEqualTo(SweepRequestOutboxStatus.PUBLISHED);
        assertThat(outbox.getPublishedAt()).isNotNull();
        assertThat(outbox.getLastErrorMessage()).isNull();
    }

    @Test
    @DisplayName("발행 실패 처리하면 FAILED 상태와 실패 메시지를 저장하고 retryCount를 증가시킨다")
    void markFailed() {
        // given
        SweepRequestOutbox outbox = createOutbox(1L);
        when(outboxRepository.findById(1L)).thenReturn(Optional.of(outbox));

        // when
        service.markFailed(1L, "sqs publish failed");

        // then
        assertThat(outbox.getStatus()).isEqualTo(SweepRequestOutboxStatus.FAILED);
        assertThat(outbox.getRetryCount()).isEqualTo(1);
        assertThat(outbox.getLastErrorMessage()).isEqualTo("sqs publish failed");
    }

    private SweepRequestOutbox createOutbox(Long outboxId) {
        CardPointLedger ledger = createLedger(outboxId);
        ledger.markSweepRequested(
                10L,
                "CARD_SWEEP:%d:2026-06".formatted(outboxId),
                LocalDateTime.now()
        );

        SweepRequestOutbox outbox = SweepRequestOutbox.ready(
                10L,
                ledger,
                "{\"eventType\":\"SWEEP_REQUESTED\"}"
        );
        setField(outbox, "outboxId", outboxId);
        return outbox;
    }

    private CardPointLedger createLedger(Long pointLedgerId) {
        try {
            Constructor<CardPointLedger> constructor = CardPointLedger.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            CardPointLedger ledger = constructor.newInstance();

            setField(ledger, "pointLedgerId", pointLedgerId);
            setField(ledger, "cardUserUuid", UUID.fromString("22222222-2222-2222-2222-222222222222"));
            setField(ledger, "performanceId", 100L);
            setField(ledger, "baseMonth", "2026-06");
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
