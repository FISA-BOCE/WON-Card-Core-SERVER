package com.woorifisa.won_card_core_server.domain.performance.exception.code;

import com.woorifisa.won_card_core_server.global.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;

public enum CardPerformanceErrorCode implements ErrorCode {

    PERFORMANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "PERFORMANCE_404_001", "전월 실적 정보가 존재하지 않습니다.");

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
