package com.woorifisa.won_card_core_server.domain.reward.service.validator;

import com.woorifisa.won_card_core_server.domain.reward.exception.code.RewardErrorCode;
import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class RewardLedgerValidator {

    public void validateRequired(UUID cardUserUuid, Long pointLedgerId) {
        if (cardUserUuid == null || pointLedgerId == null) {
            throw new BusinessException(RewardErrorCode.REWARD_LEDGER_NOT_FOUND);
        }
    }

    public void validateOwner(CardPointLedger pointLedger, UUID cardUserUuid) {
        if (!Objects.equals(pointLedger.getCardUserUuid(), cardUserUuid)) {
            throw new BusinessException(RewardErrorCode.REWARD_LEDGER_FORBIDDEN);
        }
    }
}
