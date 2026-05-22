package com.woorifisa.won_card_core_server.domain.reward.repository;

import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.RewardProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CardPointLedgerRepository extends JpaRepository<CardPointLedger, Long> {

    // 해당 카드 사용자의 현재 연도 리워드 원장 전체 조회
    @Query("""
            select l
            from CardPointLedger l
            where l.cardUserUuid = :cardUserUuid
              and l.occurredAt >= :startDateTime
              and l.occurredAt < :endDateTime
            order by l.occurredAt desc
            """)
    List<CardPointLedger> findRewardLedgers(
            @Param("cardUserUuid") UUID cardUserUuid,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    // 해당 카드 사용자의 현재 연도 리워드 원장 중 EARN / NOT_APPLIED 중 특정 상태만 조회
    @Query("""
            select l
            from CardPointLedger l
            where l.cardUserUuid = :cardUserUuid
              and l.rewardProcessStatus = :rewardProcessStatus
              and l.occurredAt >= :startDateTime
              and l.occurredAt < :endDateTime
            order by l.occurredAt desc
            """)
    List<CardPointLedger> findRewardLedgersByStatus(
            @Param("cardUserUuid") UUID cardUserUuid,
            @Param("rewardProcessStatus") RewardProcessStatus rewardProcessStatus,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    // 해당 카드 사용자의 현재 연도 적립 완료 금액 합계 조회
    @Query("""
            select coalesce(sum(l.inAmount), 0)
            from CardPointLedger l
            where l.cardUserUuid = :cardUserUuid
              and l.rewardProcessStatus = 'EARN'
              and l.occurredAt >= :startDateTime
              and l.occurredAt < :endDateTime
            """)
    BigDecimal sumEarnAmount(
            @Param("cardUserUuid") UUID cardUserUuid,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

}
