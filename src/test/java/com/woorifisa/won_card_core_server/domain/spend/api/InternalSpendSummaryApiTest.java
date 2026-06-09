package com.woorifisa.won_card_core_server.domain.spend.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.woorifisa.won_card_core_server.domain.spend.dto.response.CurrentSpendAmountResponse;
import com.woorifisa.won_card_core_server.domain.spend.exception.code.SpendErrorCode;
import com.woorifisa.won_card_core_server.domain.spend.service.SpendSummaryService;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

@WebMvcTest(InternalSpendSummaryApi.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalSpendSummaryApiTest {

    private static final UUID USER_UUID =
            UUID.fromString("0a31e4b1-2b1d-4b5e-8b82-0fb48e502111");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpendSummaryService spendSummaryService;

    @Test
    @DisplayName("당월 이용 금액 조회 API를 호출한다")
    void getSpendSummary() throws Exception {
        // given
        CurrentSpendAmountResponse response = CurrentSpendAmountResponse.found(
                "2026-05",
                1_245_000L,
                new BigDecimal("1.0"),
                "3",
                255_000L,
                new BigDecimal("1.2"),
                new CurrentSpendAmountResponse.ExpectedReward(1_245_000L, new BigDecimal("1.0"), 12_450L)
        );

        given(spendSummaryService.getSpendSummary(eq(USER_UUID))).willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/internal/cards/spend-summary")
                                .header("X-User-UUID", USER_UUID.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("CARD_200_003"))
                .andExpect(jsonPath("$.message").value("당월 이용 금액 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.hasCurrentSpendAmount").value(true))
                .andExpect(jsonPath("$.data.currentSpendAmount").value(1_245_000))
                .andExpect(jsonPath("$.data.nextPerformanceStatus").value("3"))
                .andExpect(jsonPath("$.data.expectedReward.expectedRewardAmount").value(12_450));

        then(spendSummaryService).should().getSpendSummary(USER_UUID);
    }

    @Test
    @DisplayName("이용 금액 정보가 없으면 data를 포함한 404 응답을 반환한다")
    void getSpendSummaryNotFound() throws Exception {
        // given
        given(spendSummaryService.getSpendSummary(eq(USER_UUID)))
                .willThrow(new BusinessException(SpendErrorCode.CURRENT_SPEND_AMOUNT_NOT_FOUND));

        // when & then
        mockMvc.perform(
                        get("/internal/cards/spend-summary")
                                .header("X-User-UUID", USER_UUID.toString())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("CARD_404_001"))
                .andExpect(jsonPath("$.message").value("이용 금액 정보를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
