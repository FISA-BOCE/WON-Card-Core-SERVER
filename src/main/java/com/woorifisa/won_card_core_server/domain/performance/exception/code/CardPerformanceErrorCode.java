package com.woorifisa.won_card_core_server.domain.performance.exception.code;

import com.woorifisa.won_card_core_server.global.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;

public enum CardPerformanceErrorCode implements ErrorCode {

    INVALID_PERFORMANCE_MONTH(HttpStatus.BAD_REQUEST, "CARD_400_001", "유효하지 않은 조회 월입니다."),
    PERFORMANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "CARD_404_001", "실적 정보를 찾을 수 없습니다."),
    CARD_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "CARD_404_002", "고객 정보가 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    CardPerformanceErrorCode(HttpStatus httpStatus, String code, String message) {
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
