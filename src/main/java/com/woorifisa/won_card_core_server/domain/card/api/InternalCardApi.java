package com.woorifisa.won_card_core_server.domain.card.api;

import com.woorifisa.won_card_core_server.domain.card.dto.request.CardApplicationRequest;
import com.woorifisa.won_card_core_server.domain.card.dto.response.CardApplicationResponse;
import com.woorifisa.won_card_core_server.domain.card.service.CardApplicationService;
import com.woorifisa.won_card_core_server.global.response.ApiResponse;
import com.woorifisa.won_card_core_server.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Card", description = "카드 신청, 카드 발급, 카드 상태 관리 관련 API")
@RequestMapping("/internal/cards")
public class InternalCardApi {

    private final CardApplicationService cardApplicationService;

    @Operation(summary = "계정계 카드 발급", description = "채널계 WAS에서 카드발급 요청시 카드 발급하는 API입니다.")
    @PostMapping("/applications")
    public ResponseEntity<ApiResponse<CardApplicationResponse>> cardApplication(
            @Valid @RequestBody CardApplicationRequest request
    ) {
        CardApplicationResponse response = cardApplicationService.createCardApplication(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(SuccessStatus.CARD_APPLICATION_CREATED, response));
    }
}