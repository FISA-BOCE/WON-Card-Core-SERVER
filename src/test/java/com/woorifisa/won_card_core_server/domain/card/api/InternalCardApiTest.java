package com.woorifisa.won_card_core_server.domain.card.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.woorifisa.won_card_core_server.domain.card.dto.response.CardInfoResponse;
import com.woorifisa.won_card_core_server.domain.card.service.CardApplicationService;
import com.woorifisa.won_card_core_server.domain.card.service.CardInfoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

@WebMvcTest(InternalCardApi.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalCardApiTest {

    private static final UUID USER_UUID =
            UUID.fromString("0a31e4b1-2b1d-4b5e-8b82-0fb48e502111");
    private static final UUID CARD_UUID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardApplicationService cardApplicationService;

    @MockitoBean
    private CardInfoService cardInfoService;

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