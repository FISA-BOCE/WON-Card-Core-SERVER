package com.woorifisa.won_card_core_server.domain.performance.repository;

import com.woorifisa.won_card_core_server.domain.performance.model.CardPerformance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardPerformanceRepository extends JpaRepository<CardPerformance, Long> {
    Optional<CardPerformance> findByPerformanceId(Long performanceId);
}
