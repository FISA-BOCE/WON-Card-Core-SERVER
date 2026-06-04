package com.woorifisa.won_card_core_server.domain.reward.repository;

import com.woorifisa.won_card_core_server.domain.reward.model.RewardSweepBatchExecution;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.RewardSweepBatchStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RewardSweepBatchExecutionRepository extends JpaRepository<RewardSweepBatchExecution, Long> {
    boolean existsByBaseMonthAndStatusIn(String baseMonth, List<RewardSweepBatchStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select b
            from RewardSweepBatchExecution b
            where b.batchExecutionId = :batchExecutionId
            """)
    Optional<RewardSweepBatchExecution> findByIdForUpdate(@Param("batchExecutionId") Long batchExecutionId);
}
