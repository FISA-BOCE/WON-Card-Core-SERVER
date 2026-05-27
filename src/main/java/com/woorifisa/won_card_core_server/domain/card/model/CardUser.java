package com.woorifisa.won_card_core_server.domain.card.model;

import com.woorifisa.won_card_core_server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Getter
@Entity
@Table(
        name = "card_user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_card_user_user_uuid", columnNames = "user_uuid"),
                @UniqueConstraint(name = "uk_card_user_ci_hash", columnNames = "ci_hash")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CardUser extends BaseTimeEntity {

    @Id
    @Column(name = "card_user_uuid", nullable = false, length = 36)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID cardUserUuid;

    @Column(name = "user_uuid", nullable = false, length = 36)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID userUuid;

    @Column(name = "user_name_enc", nullable = false)
    private String userNameEnc;

    @Column(name = "birth_date_enc")
    private String birthDateEnc;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 1)
    private Gender gender;

    @Column(name = "ci_hash", nullable = false)
    private String ciHash;

    @Builder.Default
    @Column(name = "nationality", nullable = false, length = 30)
    private String nationality = "KR";

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false, length = 30)
    private CardUserStatus userStatus;

    @Column(name = "is_agree", nullable = false)
    private Boolean isAgree;

    @Column(name = "tel_enc", nullable = false)
    private String telEnc;

    @Column(name = "email_enc")
    private String emailEnc;

    @Column(name = "address_enc", nullable = false, length = 1024)
    private String addressEnc;

    @PrePersist
    private void generateUuid() {
        if (cardUserUuid == null) {
            cardUserUuid = UUID.randomUUID();
        }
    }
}
