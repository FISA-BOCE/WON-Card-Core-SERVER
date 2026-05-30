package com.woorifisa.won_card_core_server.domain.reward.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RewardStatus {
    SATISFIED("기준 충족"),
    NOT_SATISFIED("기준 미달");

    private final String description;

    RewardStatus(String description) {
        this.description = description;
    }

    @JsonValue
    public String getDescription() {
        return description;
    }
}
