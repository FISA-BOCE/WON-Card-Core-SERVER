package com.woorifisa.won_card_core_server.domain.reward.model;

import com.woorifisa.won_card_core_server.domain.reward.model.enums.RewardProcessStatus;
import com.woorifisa.won_card_core_server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Table(name = "card_point_ledger")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardPointLedger extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_ledger_id")
    private Long pointLedgerId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "point_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID pointUuid;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "card_user_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID cardUserUuid;

    @Column(name = "performance_id")
    private Long performanceId;

    @Column(name = "sweep_request_id")
    private Long sweepRequestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_process_status", nullable = false, length = 30)
    private RewardProcessStatus rewardProcessStatus;

    @Column(name = "in_amount", precision = 18, scale = 4)
    private BigDecimal inAmount;

    @Column(name = "out_amount", precision = 18, scale = 4)
    private BigDecimal outAmount;

    @Column(name = "balance_after_amount", precision = 18, scale = 4)
    private BigDecimal balanceAfterAmount;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    public Long getDisplayPointAmount() {
        if (inAmount != null && inAmount.compareTo(BigDecimal.ZERO) > 0) {
            return inAmount.setScale(0, RoundingMode.DOWN).longValue();
        }

        if (outAmount != null && outAmount.compareTo(BigDecimal.ZERO) > 0) {
            return outAmount.setScale(0, RoundingMode.DOWN).longValue();
        }

        return 0L;
    }

    public String getBaseMonth() {
        return occurredAt.getYear() + "-" + String.format("%02d", occurredAt.getMonthValue());
    }
}
