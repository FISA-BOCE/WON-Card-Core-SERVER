package com.woorifisa.won_card_core_server.domain.performance.model;

import com.woorifisa.won_card_core_server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "card_performance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardPerformance extends BaseTimeEntity {

    @Id
    @Column(name = "performance_id")
    private Long performanceId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "user_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID userUuid;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "card_user_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID cardUserUuid;

    @Column(name = "base_month", nullable = false, length = 7)
    private String baseMonth;

    @Column(name = "previous_month_spend_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal previousMonthSpendAmount;

    @Column(name = "current_month_spend_amount", precision = 18, scale = 4)
    private BigDecimal currentMonthSpendAmount;

    @Column(name = "reward_rate", nullable = false, precision = 10, scale = 6)
    private BigDecimal rewardRate;

    @Column(name = "reward_point_amount", precision = 18, scale = 4)
    private BigDecimal rewardPointAmount;

    @Column(name = "limit_apply_status", length = 30)
    private String limitApplyStatus;

    @Column(name = "performance_status", length = 30)
    private String performanceStatus;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    public Long getPreviousMonthSpendAmountAsLong() {
        return previousMonthSpendAmount == null
                ? 0L
                : previousMonthSpendAmount.setScale(0, java.math.RoundingMode.DOWN).longValue();
    }

}
