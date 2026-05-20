package com.woorifisa.won_card_core_server.domain.reward.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardLedgerResponse;
import com.woorifisa.won_card_core_server.domain.reward.exception.RewardErrorCode;
import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.RewardProcessStatus;
import com.woorifisa.won_card_core_server.domain.reward.repository.CardPointLedgerRepository;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RewardLedgerServiceTest {

    private static final UUID CARD_USER_UUID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final UUID POINT_UUID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private CardPointLedgerRepository cardPointLedgerRepository;

    @InjectMocks
    private RewardLedgerService rewardLedgerService;

    @Test
    @DisplayName("type이 없으면 전체 리워드 내역을 조회한다")
    void getRewardLedgerAll() throws Exception {
        // given
        CardPointLedger earnLedger = newCardPointLedger(
                1001L,
                RewardProcessStatus.EARN,
                BigDecimal.valueOf(12450),
                null,
                LocalDateTime.of(2026, 5, 7, 14, 32)
        );

        CardPointLedger holdLedger = newCardPointLedger(
                1002L,
                RewardProcessStatus.HOLD,
                BigDecimal.valueOf(5000),
                null,
                LocalDateTime.of(2026, 5, 6, 14, 32)
        );

        given(cardPointLedgerRepository.sumEarnAmount(
                eq(CARD_USER_UUID), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(BigDecimal.valueOf(12450));

        given(cardPointLedgerRepository.findRewardLedgers(
                eq(CARD_USER_UUID), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(List.of(earnLedger, holdLedger));

        // when
        RewardLedgerResponse response = rewardLedgerService.getRewardLedger(CARD_USER_UUID, null);

        // then
        assertThat(response.baseYear()).isGreaterThanOrEqualTo(2026);
        assertThat(response.totalAccumulatedAmount()).isEqualTo(12450L);
        assertThat(response.ledgers()).hasSize(2);

        assertThat(response.ledgers().get(0).pointLedgerId()).isEqualTo(1001L);
        assertThat(response.ledgers().get(0).pointAmount()).isEqualTo(12450L);
        assertThat(response.ledgers().get(0).type()).isEqualTo("EARN");

        then(cardPointLedgerRepository).should().findRewardLedgers(
                eq(CARD_USER_UUID), any(LocalDateTime.class), any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("type이 EARN이면 적립 완료 내역만 조회한다")
    void getRewardLedgerEarn() throws Exception {
        // given
        CardPointLedger earnLedger = newCardPointLedger(
                1001L, RewardProcessStatus.EARN, BigDecimal.valueOf(12450), null,
                LocalDateTime.of(2026, 5, 7, 14, 32)
        );

        given(cardPointLedgerRepository.sumEarnAmount(
                eq(CARD_USER_UUID), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(BigDecimal.valueOf(12450));

        given(cardPointLedgerRepository.findRewardLedgersByStatus(
                eq(CARD_USER_UUID), eq(RewardProcessStatus.EARN), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(List.of(earnLedger));

        // when
        RewardLedgerResponse response = rewardLedgerService.getRewardLedger(CARD_USER_UUID, "EARN");

        // then
        assertThat(response.totalAccumulatedAmount()).isEqualTo(12450L);
        assertThat(response.ledgers()).hasSize(1);
        assertThat(response.ledgers().get(0).type()).isEqualTo("EARN");

        then(cardPointLedgerRepository).should().findRewardLedgersByStatus(
                eq(CARD_USER_UUID),
                eq(RewardProcessStatus.EARN),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("리워드 내역이 없으면 예외가 발생한다")
    void getRewardLedgerNotFound() {
        // given
        given(cardPointLedgerRepository.sumEarnAmount(
                eq(CARD_USER_UUID),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).willReturn(BigDecimal.ZERO);

        given(cardPointLedgerRepository.findRewardLedgers(
                eq(CARD_USER_UUID),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> rewardLedgerService.getRewardLedger(CARD_USER_UUID, null))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode())
                            .isEqualTo(RewardErrorCode.REWARD_LEDGER_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("유효하지 않은 type이면 예외가 발생한다")
    void getRewardLedgerInvalidType() {
        assertThatThrownBy(() -> rewardLedgerService.getRewardLedger(CARD_USER_UUID, "BAD"))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode())
                            .isEqualTo(RewardErrorCode.INVALID_REWARD_LEDGER_TYPE);
                });
    }

    private CardPointLedger newCardPointLedger(
            Long pointLedgerId,
            RewardProcessStatus rewardProcessStatus,
            BigDecimal inAmount,
            BigDecimal outAmount,
            LocalDateTime occurredAt
    ) throws Exception {
        Constructor<CardPointLedger> constructor = CardPointLedger.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        CardPointLedger ledger = constructor.newInstance();
        setField(ledger, "pointLedgerId", pointLedgerId);
        setField(ledger, "pointUuid", POINT_UUID);
        setField(ledger, "cardUserUuid", CARD_USER_UUID);
        setField(ledger, "rewardProcessStatus", rewardProcessStatus);
        setField(ledger, "inAmount", inAmount);
        setField(ledger, "outAmount", outAmount);
        setField(ledger, "balanceAfterAmount", BigDecimal.valueOf(1245000));
        setField(ledger, "occurredAt", occurredAt);

        return ledger;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
