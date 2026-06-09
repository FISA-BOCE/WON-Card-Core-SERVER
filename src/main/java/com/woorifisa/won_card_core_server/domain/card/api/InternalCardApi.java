package com.woorifisa.won_card_core_server.domain.card.api;

import com.woorifisa.won_card_core_server.domain.card.dto.request.CardApplicationRequest;
import com.woorifisa.won_card_core_server.domain.card.dto.response.CardApplicationResponse;
import com.woorifisa.won_card_core_server.domain.card.dto.response.CardInfoResponse;
import com.woorifisa.won_card_core_server.domain.card.service.CardApplicationService;
import com.woorifisa.won_card_core_server.domain.card.service.CardInfoService;
import com.woorifisa.won_card_core_server.global.response.ApiResponse;
import com.woorifisa.won_card_core_server.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Card", description = "카드 신청, 카드 발급, 카드 상태 관리 관련 API")
@RequestMapping("/internal/cards")
public class InternalCardApi {

    private final CardApplicationService cardApplicationService;
    private final CardInfoService cardInfoService;

    @Operation(summary = "카드 정보 조회", description = "채널계 WAS에서 카드 사용자 UUID로 발급 카드 정보를 조회하는 API입니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<CardInfoResponse>> getCardInfo(
            @RequestHeader("X-User-UUID") UUID userUuid
    ) {
        CardInfoResponse response = cardInfoService.getCardInfo(userUuid);
        SuccessStatus successStatus = response.hasCard()
                ? SuccessStatus.CARD_INFO_FOUND
                : SuccessStatus.CARD_INFO_NOT_FOUND;

        return ResponseEntity
                .status(successStatus.getHttpStatus())
                .body(ApiResponse.of(successStatus, response));
    }

    @Operation(summary = "계정 카드 발급", description = "채널계 WAS에서 카드 발급 요청 시 카드를 발급하는 API입니다.")
    @PostMapping("/applications")
    public ResponseEntity<ApiResponse<CardApplicationResponse>> cardApplication(
            @RequestHeader("X-User-UUID") UUID userUuid,
            @Valid @RequestBody CardApplicationRequest request
    ) {
        CardApplicationResponse response = cardApplicationService.createCardApplication(userUuid, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(SuccessStatus.CARD_APPLICATION_CREATED, response));
    }
}
