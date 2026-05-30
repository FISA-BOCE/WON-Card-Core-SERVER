package com.woorifisa.won_card_core_server.domain.card.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorifisa.won_card_core_server.domain.card.dto.request.CardApplicationRequest;
import com.woorifisa.won_card_core_server.domain.card.dto.response.CardApplicationResponse;
import com.woorifisa.won_card_core_server.domain.card.dto.response.CardInfoResponse;
import com.woorifisa.won_card_core_server.domain.card.model.Gender;
import com.woorifisa.won_card_core_server.domain.card.service.CardApplicationService;
import com.woorifisa.won_card_core_server.domain.card.service.CardInfoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

@WebMvcTest(InternalCardApi.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalCardApiTest {

    private static final UUID USER_UUID =
            UUID.fromString("0a31e4b1-2b1d-4b5e-8b82-0fb48e502111");
    private static final UUID CARD_USER_UUID =
            UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID CARD_UUID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardApplicationService cardApplicationService;

    @MockitoBean
    private CardInfoService cardInfoService;

    @Test
    @DisplayName("X-User-UUID 헤더로 카드 신청 API를 호출하면 cardUserUuid를 포함해 반환한다")
    void cardApplication() throws Exception {
        CardApplicationRequest request = new CardApplicationRequest(
                "encrypted-name",
                "encrypted-birth-date",
                Gender.M,
                "KR",
                true,
                "encrypted-tel",
                "encrypted-email",
                "encrypted-address"
        );
        CardApplicationResponse response = new CardApplicationResponse(
                CARD_USER_UUID,
                CARD_UUID,
                "****-****-****-1234",
                LocalDateTime.of(2026, 5, 25, 23, 42, 29),
                "ACTIVE"
        );

        given(cardApplicationService.createCardApplication(eq(USER_UUID), eq(request))).willReturn(response);

        mockMvc.perform(
                        post("/internal/cards/applications")
                                .header("X-User-UUID", USER_UUID.toString())
                                .header("X-Service-ID", "WON-CARD-CHANNEL")
                                .header("X-Transaction-ID", "TX-20260526-CARD00")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value("CARD_201_001"))
                .andExpect(jsonPath("$.message").value("카드 신청이 완료되었습니다."))
                .andExpect(jsonPath("$.data.cardUserUuid").value(CARD_USER_UUID.toString()))
                .andExpect(jsonPath("$.data.cardUuid").value(CARD_UUID.toString()))
                .andExpect(jsonPath("$.data.cardNoDisplay").value("****-****-****-1234"))
                .andExpect(jsonPath("$.data.cardStatus").value("ACTIVE"));

        then(cardApplicationService).should().createCardApplication(USER_UUID, request);
    }

    @Test
    @DisplayName("X-User-UUID 헤더로 카드 정보 조회 API를 호출한다")
    void getCardInfo() throws Exception {
        // given
        CardInfoResponse response = new CardInfoResponse(
                true,
                CARD_UUID,
                "****-****-****-1234",
                "ACTIVE",
                new CardInfoResponse.UsageSummary(1245000L)
        );

        given(cardInfoService.getCardInfo(eq(USER_UUID))).willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/internal/cards")
                                .header("X-User-UUID", USER_UUID.toString())
                                .header("X-Service-ID", "WON-CARD-CHANNEL")
                                .header("X-Transaction-ID", "TX-20260526-CARD01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("카드 정보 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.hasCard").value(true))
                .andExpect(jsonPath("$.data.cardUuid").value(CARD_UUID.toString()))
                .andExpect(jsonPath("$.data.cardNoDisplay").value("****-****-****-1234"))
                .andExpect(jsonPath("$.data.cardStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.usageSummary.currentMonthUsageAmount").value(1245000));

        then(cardInfoService).should().getCardInfo(USER_UUID);
    }

    @Test
    @DisplayName("발급 카드가 없으면 카드 없음 메시지를 반환한다")
    void getCardInfoNoCard() throws Exception {
        // given
        given(cardInfoService.getCardInfo(eq(USER_UUID))).willReturn(CardInfoResponse.noCard());

        // when & then
        mockMvc.perform(
                        get("/internal/cards")
                                .header("X-User-UUID", USER_UUID.toString())
                                .header("X-Service-ID", "WON-CARD-CHANNEL")
                                .header("X-Transaction-ID", "TX-20260526-CARD02")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("신청된 카드 정보가 없습니다."))
                .andExpect(jsonPath("$.data.hasCard").value(false))
                .andExpect(jsonPath("$.data.cardUuid").doesNotExist())
                .andExpect(jsonPath("$.data.usageSummary").doesNotExist());
    }
}
