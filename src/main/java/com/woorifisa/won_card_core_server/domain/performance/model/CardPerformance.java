package com.woorifisa.won_card_core_server.domain.performance.model;

import com.woorifisa.won_card_core_server.domain.card.model.CardUser;
import com.woorifisa.won_card_core_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(
        name = "card_performance",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_card_performance_card_user_base_month",
                        columnNames = {"card_user_uuid", "base_month"}
                )
        },
        indexes = {
                @Index(name = "idx_card_performance_user_uuid", columnList = "user_uuid"),
                @Index(name = "idx_card_performance_card_user_uuid", columnList = "card_user_uuid"),
                @Index(name = "idx_card_performance_base_month", columnList = "base_month")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CardPerformance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "performance_id", nullable = false)
    private Long performanceId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_uuid", nullable = false, length = 36)
    private UUID userUuid;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "card_user_uuid", nullable = false, length = 36)
    private UUID cardUserUuid;

    @Column(name = "base_month", nullable = false, length = 7)
    private String baseMonth;

    @Builder.Default
    @Column(name = "previous_month_spend_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal previousMonthSpendAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "current_month_spend_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal currentMonthSpendAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "reward_rate", nullable = false, precision = 10, scale = 6)
    private BigDecimal rewardRate = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "reward_point_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal rewardPointAmount = BigDecimal.ZERO;

    @Column(name = "limit_apply_status", nullable = false, length = 30)
    private String limitApplyStatus;

    @Column(name = "performance_status", nullable = false, length = 30)
    private String performanceStatus;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "card_user_uuid",
            referencedColumnName = "card_user_uuid",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_card_performance_card_user")
    )
    private CardUser cardUser;

}
