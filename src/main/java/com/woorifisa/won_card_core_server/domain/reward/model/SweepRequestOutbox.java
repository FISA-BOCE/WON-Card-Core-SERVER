package com.woorifisa.won_card_core_server.domain.reward.model;

import com.woorifisa.won_card_core_server.domain.reward.model.enums.SweepRequestOutboxStatus;
import com.woorifisa.won_card_core_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "sweep_request_outbox",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_sweep_request_outbox_idempotency_key",
                columnNames = "idempotency_key"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SweepRequestOutbox extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbox_id")
    private Long outboxId;

    @Column(name = "batch_execution_id", nullable = false)
    private Long batchExecutionId;

    @Column(name = "point_ledger_id", nullable = false)
    private Long pointLedgerId;

    @Column(name = "sweep_request_id", nullable = false)
    private Long sweepRequestId;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "card_user_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID cardUserUuid;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SweepRequestOutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error_message", length = 500)
    private String lastErrorMessage;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public static SweepRequestOutbox ready(
            Long batchExecutionId,
            CardPointLedger ledger,
            String payload
    ) {
        SweepRequestOutbox outbox = new SweepRequestOutbox();
        outbox.batchExecutionId = batchExecutionId;
        outbox.pointLedgerId = ledger.getPointLedgerId();
        outbox.sweepRequestId = ledger.getSweepRequestId();
        outbox.idempotencyKey = ledger.getIdempotencyKey();
        outbox.cardUserUuid = ledger.getCardUserUuid();
        outbox.payload = payload;
        outbox.status = SweepRequestOutboxStatus.READY;
        return outbox;
    }

    public void markPublishing() {
        this.status = SweepRequestOutboxStatus.PUBLISHING;
    }

    public void markPublished() {
        this.status = SweepRequestOutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.lastErrorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = SweepRequestOutboxStatus.FAILED;
        this.retryCount++;
        this.lastErrorMessage = errorMessage;
    }

    public void retry() {
        this.status = SweepRequestOutboxStatus.READY;
    }
}
