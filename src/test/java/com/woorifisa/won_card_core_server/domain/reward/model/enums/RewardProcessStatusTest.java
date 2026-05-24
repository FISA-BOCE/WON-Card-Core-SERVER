package com.woorifisa.won_card_core_server.domain.reward.model.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.woorifisa.won_card_core_server.domain.reward.exception.code.RewardErrorCode;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RewardProcessStatusTest {

    @Test
    @DisplayName("type 값이 없으면 ALL을 반환한다")
    void fromNull() {
        assertThat(RewardProcessStatus.from(null)).isEqualTo(RewardProcessStatus.ALL);
        assertThat(RewardProcessStatus.from("")).isEqualTo(RewardProcessStatus.ALL);
        assertThat(RewardProcessStatus.from(" ")).isEqualTo(RewardProcessStatus.ALL);
    }

    @Test
    @DisplayName("유효한 type 값을 RewardProcessStatus로 변환한다")
    void fromValidType() {
        assertThat(RewardProcessStatus.from("EARN")).isEqualTo(RewardProcessStatus.EARN);
        assertThat(RewardProcessStatus.from("NOT_APPLIED")).isEqualTo(RewardProcessStatus.NOT_APPLIED);
        assertThat(RewardProcessStatus.from("earn")).isEqualTo(RewardProcessStatus.EARN);
    }

    @Test
    @DisplayName("유효하지 않은 type 값이면 예외가 발생한다")
    void fromInvalidType() {
        assertThatThrownBy(() -> RewardProcessStatus.from("BAD"))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode())
                            .isEqualTo(RewardErrorCode.INVALID_REWARD_LEDGER_TYPE);
                });
    }
}
