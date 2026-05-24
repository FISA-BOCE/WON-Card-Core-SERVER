package com.woorifisa.won_card_core_server.domain.card.model;

import com.woorifisa.won_card_core_server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "card",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_card_card_no_token", columnNames = "card_no_token")
        },
        indexes = {
                @Index(name = "idx_card_card_user_uuid", columnList = "card_user_uuid"),
                @Index(name = "idx_card_status", columnList = "card_status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Card extends BaseTimeEntity {

    @Id
    @Column(name = "card_uuid", nullable = false, length = 36)
    private String cardUuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "card_user_uuid",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_card_card_user")
    )
    private CardUser cardUser;

    @Column(name = "card_no_token", nullable = false)
    private String cardNoToken;

    @Column(name = "card_no_display", length = 50)
    private String cardNoDisplay;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_status", nullable = false, length = 30)
    private CardStatus cardStatus;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Builder.Default
    @Column(name = "total_limit_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal totalLimitAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "available_limit_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal availableLimitAmount = BigDecimal.ZERO;

    @PrePersist
    private void generateUuid() {
        if (cardUuid == null) {
            cardUuid = UUID.randomUUID().toString();
        }
    }
}
