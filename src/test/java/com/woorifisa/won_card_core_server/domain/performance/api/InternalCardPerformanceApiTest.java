package com.woorifisa.won_card_core_server.domain.performance.api;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.woorifisa.won_card_core_server.domain.performance.dto.response.PreviousPerformanceResponse;
import com.woorifisa.won_card_core_server.domain.performance.exception.code.CardPerformanceErrorCode;
import com.woorifisa.won_card_core_server.domain.performance.service.CardPerformanceService;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

@WebMvcTest(InternalCardPerformanceApi.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalCardPerformanceApiTest {

    private static final UUID USER_UUID =
            UUID.fromString("0a31e4b1-2b1d-4b5e-8b82-0fb48e502111");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardPerformanceService cardPerformanceService;

    @Test
    void getPreviousPerformance() throws Exception {
        PreviousPerformanceResponse response = new PreviousPerformanceResponse(
                "2026-05",
                "기준 충족",
                820000L,
                8200L,
                BigDecimal.ONE,
                "2"
        );

        given(cardPerformanceService.getPreviousPerformance(USER_UUID)).willReturn(response);

        mockMvc.perform(
                        get("/internal/cards/performance/monthly")
                                .header("X-User-UUID", USER_UUID.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("CARD_200_004"))
                .andExpect(jsonPath("$.message").value("전월 실적 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.baseMonth").value("2026-05"))
                .andExpect(jsonPath("$.data.rewardStatus").value("기준 충족"))
                .andExpect(jsonPath("$.data.previousMonthSpendAmount").value(820000))
                .andExpect(jsonPath("$.data.rewardPointAmount").value(8200))
                .andExpect(jsonPath("$.data.rewardRate").value(1))
                .andExpect(jsonPath("$.data.performanceStatus").value("2"));

        then(cardPerformanceService).should().getPreviousPerformance(USER_UUID);
    }

    @Test
    void getPreviousPerformanceNotFound() throws Exception {
        given(cardPerformanceService.getPreviousPerformance(USER_UUID))
                .willThrow(new BusinessException(CardPerformanceErrorCode.PERFORMANCE_NOT_FOUND));

        mockMvc.perform(
                        get("/internal/cards/performance/monthly")
                                .header("X-User-UUID", USER_UUID.toString())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("CARD_404_001"))
                .andExpect(jsonPath("$.message").value("실적 정보를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
