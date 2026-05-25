package com.woorifisa.won_card_core_server.domain.card.repository;

import com.woorifisa.won_card_core_server.domain.card.model.CardUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CardUserRepository extends JpaRepository<CardUser, String> {

    @Query("select cu from CardUser cu where cu.userUuid = :userUuid")
    Optional<CardUser> findByUserUuid(@Param("userUuid") String userUuid);

    @Query("select count(cu) > 0 from CardUser cu where cu.ciHash = :ciHash")
    boolean existsByCiHash(@Param("ciHash") String ciHash);
}