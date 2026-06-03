package com.woorifisa.won_card_core_server.domain.reward.service;

import com.woorifisa.won_card_core_server.domain.reward.dto.response.SweepRequestOutboxResponse;
import com.woorifisa.won_card_core_server.domain.reward.model.SweepRequestOutbox;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.SweepRequestOutboxStatus;
import com.woorifisa.won_card_core_server.domain.reward.repository.SweepRequestOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SweepRequestOutboxService {

    private final SweepRequestOutboxRepository outboxRepository;

    @Transactional
    public List<SweepRequestOutboxResponse> claimReady(int size) {
        List<SweepRequestOutbox> outboxes = outboxRepository.findByStatusOrderByOutboxIdAsc(
                SweepRequestOutboxStatus.READY,
                PageRequest.of(0, size)
        );

        outboxes.forEach(SweepRequestOutbox::markPublishing);

        return outboxes.stream()
                .map(SweepRequestOutboxResponse::from)
                .toList();
    }

    @Transactional
    public void markPublished(Long outboxId) {
        SweepRequestOutbox outbox = outboxRepository.findById(outboxId)
                .orElseThrow();

        outbox.markPublished();
    }

    @Transactional
    public void markFailed(Long outboxId, String errorMessage) {
        SweepRequestOutbox outbox = outboxRepository.findById(outboxId)
                .orElseThrow();

        outbox.markFailed(errorMessage);
    }
}
