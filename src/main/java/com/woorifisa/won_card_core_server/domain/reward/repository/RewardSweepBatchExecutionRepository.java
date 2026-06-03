package com.woorifisa.won_card_core_server.domain.reward.repository;

import com.woorifisa.won_card_core_server.domain.reward.model.RewardSweepBatchExecution;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.RewardSweepBatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardSweepBatchExecutionRepository extends JpaRepository<RewardSweepBatchExecution, Long> {
    boolean existsByBaseMonthAndStatusIn(String baseMonth, List<RewardSweepBatchStatus> statuses);
}
