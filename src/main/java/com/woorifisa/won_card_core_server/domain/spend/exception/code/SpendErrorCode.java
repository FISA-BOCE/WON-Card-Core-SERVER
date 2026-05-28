package com.woorifisa.won_card_core_server.domain.spend.exception.code;

import com.woorifisa.won_card_core_server.global.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;

public enum SpendErrorCode implements ErrorCode {

    CURRENT_SPEND_AMOUNT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "CARD_404_001",
            "이용 금액 정보를 찾을 수 없습니다."
    ),
    CARD_USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "CARD_404_002",
            "고객 정보가 존재하지 않습니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    SpendErrorCode(HttpStatus httpStatus, String code, String message) {
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
