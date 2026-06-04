package com.woorifisa.won_card_core_server.domain.reward.model;

import com.woorifisa.won_card_core_server.domain.reward.model.enums.RewardSweepBatchStatus;
import com.woorifisa.won_card_core_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "reward_sweep_batch_execution")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RewardSweepBatchExecution extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_execution_id")
    private Long batchExecutionId;

    @Column(name = "base_month", nullable = false, length = 7)
    private String baseMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RewardSweepBatchStatus status;

    @Column(name = "total_candidate_count", nullable = false)
    private long totalCandidateCount;

    @Column(name = "requested_count", nullable = false)
    private long requestedCount;

    @Column(name = "completed_count", nullable = false)
    private long completedCount;

    @Column(name = "failed_count", nullable = false)
    private long failedCount;

    @Column(name = "timeout_count", nullable = false)
    private long timeoutCount;

    @Column(name = "last_processed_point_ledger_id")
    private Long lastProcessedPointLedgerId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failure_message", length = 500)
    private String failureMessage;

    public static RewardSweepBatchExecution start(String baseMonth, LocalDateTime startedAt) {
        RewardSweepBatchExecution batch = new RewardSweepBatchExecution();
        batch.baseMonth = baseMonth;
        batch.status = RewardSweepBatchStatus.RUNNING;
        batch.startedAt = startedAt;
        batch.lastProcessedPointLedgerId = 0L;
        return batch;
    }

    public void addRequested(int count, Long lastProcessedPointLedgerId) {
        this.totalCandidateCount += count;
        this.requestedCount += count;
        this.lastProcessedPointLedgerId = lastProcessedPointLedgerId;
    }

    public void increaseCompleted() {
        this.completedCount++;
        completeIfFinished();
    }

    public void increaseFailed() {
        this.failedCount++;
        completeIfFinished();
    }

    private void completeIfFinished() {
        if (completedCount + failedCount < totalCandidateCount) {
            return;
        }

        if (failedCount == 0) {
            this.status = RewardSweepBatchStatus.COMPLETED;
        } else if (completedCount == 0) {
            this.status = RewardSweepBatchStatus.FAILED;
        } else {
            this.status = RewardSweepBatchStatus.PARTIALLY_FAILED;
        }
        this.completedAt = LocalDateTime.now();
    }

    public void completeWhenNoCandidates() {
        if (this.totalCandidateCount != 0) {
            return;
        }

        this.status = RewardSweepBatchStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
