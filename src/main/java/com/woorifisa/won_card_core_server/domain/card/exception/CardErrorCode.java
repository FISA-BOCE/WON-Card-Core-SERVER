package com.woorifisa.won_card_core_server.domain.card.exception;

import com.woorifisa.won_card_core_server.global.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;

public enum CardErrorCode implements ErrorCode {

    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "CARD_400_001", "필수 약관에 동의하지 않았습니다."),
    CARD_ALREADY_EXISTS(HttpStatus.CONFLICT, "CARD_409_001", "이미 발급된 카드가 있습니다."),
    CARD_USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "CARD_409_002", "이미 등록된 카드 고객입니다."),
    CARD_CONSTRAINT_CONFLICT(HttpStatus.CONFLICT, "CARD_409_003", "제약사항 충돌"),
    CARD_ISSUANCE_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY, "CARD_422_001", "카드발급 불가");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    CardErrorCode(HttpStatus httpStatus, String code, String message) {
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