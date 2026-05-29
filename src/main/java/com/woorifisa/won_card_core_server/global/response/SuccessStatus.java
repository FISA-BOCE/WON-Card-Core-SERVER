package com.woorifisa.won_card_core_server.global.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SuccessStatus {

    OK(HttpStatus.OK, "COM_200_001", "OK"),
    CREATED(HttpStatus.CREATED, "COM_201_001", "CREATED"),
    NO_CONTENT(HttpStatus.NO_CONTENT, "COM_204_001", "NO_CONTENT"),

    CARD_APPLICATION_CREATED(HttpStatus.CREATED, "CARD_201_001", "카드 신청이 완료되었습니다."),
    CARD_INFO_FOUND(HttpStatus.OK, "CARD_200_001", "카드 정보 조회가 완료되었습니다."),
    CARD_INFO_NOT_FOUND(HttpStatus.OK, "CARD_200_002", "신청된 카드 정보가 없습니다."),
    CURRENT_SPEND_AMOUNT_FOUND(HttpStatus.OK, "CARD_200_003", "당월 이용 금액 조회가 완료되었습니다."),
    PREVIOUS_PERFORMANCE_FOUND(HttpStatus.OK, "CARD_200_004", "전월 실적 조회가 완료되었습니다."),

    REWARD_LEDGER_FOUND(HttpStatus.OK, "REWARD_200_001", "리워드 내역 조회가 완료되었습니다."),
    REWARD_LEDGER_DETAIL_FOUND(HttpStatus.OK, "REWARD_200_002", "상세 리워드 내역 조회가 완료되었습니다."),

    REWARD_SWEEP_REQUESTED(HttpStatus.OK, "SWEEP_200_001", "스윕 요청 선점이 완료되었습니다."),
    REWARD_SWEEP_CANDIDATES_FOUND(HttpStatus.OK, "SWEEP_200_002", "스윕 후보 리워드 원장 조회가 완료되었습니다."),
    REWARD_SWEEP_REQUEST_CANCELLED(HttpStatus.OK, "SWEEP_200_003", "리워드 원장 스윕 요청이 취소되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    SuccessStatus(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
