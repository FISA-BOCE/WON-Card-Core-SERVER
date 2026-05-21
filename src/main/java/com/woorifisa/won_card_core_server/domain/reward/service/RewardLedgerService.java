package com.woorifisa.won_card_core_server.domain.reward.service;

import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardLedgerResponse;
import com.woorifisa.won_card_core_server.domain.reward.model.CardPointLedger;
import com.woorifisa.won_card_core_server.domain.reward.model.enums.RewardProcessStatus;
import com.woorifisa.won_card_core_server.domain.reward.repository.CardPointLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardLedgerService {

    private final CardPointLedgerRepository cardPointLedgerRepository;

    public RewardLedgerResponse getRewardLedger(UUID cardUserUuid, String type) {
        RewardProcessStatus rewardProcessStatus = RewardProcessStatus.from(type);

        int baseYear = Year.now().getValue();
        LocalDateTime startDateTime = LocalDateTime.of(baseYear, 1, 1, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(baseYear + 1, 1, 1, 0, 0);

        Long totalAccumulatedAmount = getTotalAccumulatedAmount(cardUserUuid, startDateTime, endDateTime);

        List<CardPointLedger> ledgers = getLedgers(cardUserUuid, rewardProcessStatus, startDateTime, endDateTime);

        return new RewardLedgerResponse(baseYear, totalAccumulatedAmount,
                ledgers.stream()
                        .map(RewardLedgerResponse.RewardLedgerItem::from)
                        .toList()
        );
    }

    private Long getTotalAccumulatedAmount(UUID cardUserUuid, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        BigDecimal totalAmount = cardPointLedgerRepository.sumEarnAmount(cardUserUuid, startDateTime, endDateTime);

        return totalAmount.setScale(0, RoundingMode.DOWN).longValue();
    }

    private List<CardPointLedger> getLedgers(UUID cardUserUuid, RewardProcessStatus rewardProcessStatus,
                                             LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (rewardProcessStatus.isAll()) {
            return cardPointLedgerRepository.findRewardLedgers(cardUserUuid, startDateTime, endDateTime);
        }

        return cardPointLedgerRepository.findRewardLedgersByStatus(cardUserUuid, rewardProcessStatus, startDateTime, endDateTime);
    }
}
