package com.woorifisa.won_card_core_server.domain.card.repository;

import com.woorifisa.won_card_core_server.domain.card.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, String> {

    @Query("select c from Card c where c.cardUser.cardUserUuid = :cardUserUuid")
    List<Card> findAllByCardUserUuid(@Param("cardUserUuid") String cardUserUuid);

    @Query("select count(c) > 0 from Card c where c.cardUser.cardUserUuid = :cardUserUuid")
    boolean existsByCardUserUuid(@Param("cardUserUuid") String cardUserUuid);

    @Query("select count(c) > 0 from Card c where c.cardNoToken = :cardNoToken")
    boolean existsByCardNoToken(@Param("cardNoToken") String cardNoToken);
}