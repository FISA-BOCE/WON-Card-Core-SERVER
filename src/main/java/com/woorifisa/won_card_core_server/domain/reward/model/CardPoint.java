package com.woorifisa.won_card_core_server.domain.reward.model;

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
import java.util.UUID;

@Entity
@Table(name = "card_point")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardPoint {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "point_uuid", columnDefinition = "CHAR(36)")
    private UUID pointUuid;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "card_user_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID cardUserUuid;

    @Column(name = "point_balance_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal pointBalanceAmount;

}
