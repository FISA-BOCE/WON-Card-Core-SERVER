package com.woorifisa.won_card_core_server.domain.reward.repository;

import com.woorifisa.won_card_core_server.domain.reward.model.SweepRequestOutbox;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.SweepRequestOutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SweepRequestOutboxRepository extends JpaRepository<SweepRequestOutbox, Long> {
    List<SweepRequestOutbox> findByStatusOrderByOutboxIdAsc(SweepRequestOutboxStatus status, Pageable pageable);
}

