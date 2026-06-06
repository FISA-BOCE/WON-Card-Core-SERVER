package com.woorifisa.won_card_core_server.domain.reward.exception.code;

import com.woorifisa.won_card_core_server.global.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;

public enum RewardErrorCode implements ErrorCode {

    INVALID_REWARD_LEDGER_TYPE(HttpStatus.BAD_REQUEST, "REWARD_400_001", "유효하지 않은 type 값입니다."),
    INVALID_REWARD_BASE_MONTH(HttpStatus.BAD_REQUEST, "REWARD_400_002", "유효하지 않은 기준월입니다."),
    INVALID_REWARD_SWEEP_BATCH_SIZE(HttpStatus.BAD_REQUEST, "REWARD_400_003", "스윕 배치 chunkSize가 올바르지 않습니다."),

    INVALID_PERFORMANCE_AMOUNT(HttpStatus.BAD_REQUEST, "CARD_400_002", "금액 형식이 올바르지 않습니다."),
    CARD_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "CARD_404_002", "고객 정보가 존재하지 않습니다."),

    REWARD_LEDGER_FORBIDDEN(HttpStatus.FORBIDDEN, "REWARD_403_001", "본인의 리워드 내역이 아닙니다."),

    REWARD_LEDGER_NOT_FOUND(HttpStatus.NOT_FOUND, "REWARD_404_001", "리워드 내역을 찾을 수 없습니다."),
    REWARD_SWEEP_BATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "REWARD_404_002", "스윕 배치 실행 이력을 찾을 수 없습니다."),

    REWARD_SWEEP_ALREADY_REQUESTED(HttpStatus.CONFLICT, "REWARD_409_001", "이미 스윕 요청된 리워드 원장입니다."),
    REWARD_SWEEP_BATCH_ALREADY_RUNNING(HttpStatus.CONFLICT, "REWARD_409_002", "이미 실행 중인 스윕 배치가 있습니다."),

    REWARD_SWEEP_NOT_ELIGIBLE(HttpStatus.UNPROCESSABLE_ENTITY, "REWARD_422_001", "스윕할 수 없는 리워드 원장입니다."),
    REWARD_SWEEP_AMOUNT_INVALID(HttpStatus.UNPROCESSABLE_ENTITY, "REWARD_422_002", "스윕 가능한 리워드 금액이 없습니다."),
    REWARD_SWEEP_CANCEL_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY, "REWARD_422_003", "취소할 수 없는 스윕 요청 상태입니다."),
    REWARD_SWEEP_BATCH_NOT_RUNNING(HttpStatus.UNPROCESSABLE_ENTITY, "REWARD_422_004", "실행 중인 스윕 배치가 아닙니다."),

    INVALID_REWARD_LEDGER_STATUS(HttpStatus.INTERNAL_SERVER_ERROR, "REWARD_500_001", "리워드 내역 상태가 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    RewardErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
