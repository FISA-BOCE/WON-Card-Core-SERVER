package com.woorifisa.won_card_core_server.domain.performance.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.woorifisa.won_card_core_server.domain.performance.dto.response.PreviousPerformanceResponse;
import com.woorifisa.won_card_core_server.domain.performance.exception.code.CardPerformanceErrorCode;
import com.woorifisa.won_card_core_server.domain.performance.service.CardPerformanceService;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
    @DisplayName("전월 실적 조회 API를 호출한다")
    void getPreviousPerformance() throws Exception {
        // given
        PreviousPerformanceResponse response = new PreviousPerformanceResponse(
                "2026-05",
                "기준 충족",
                820000L,
                new PreviousPerformanceResponse.PerformanceDetail(820000L, 8200L)
        );

        given(cardPerformanceService.getPreviousPerformance(eq(USER_UUID), eq("2026-04")))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/internal/cards/performance/monthly")
                                .header("X-User-UUID", USER_UUID.toString())
                                .param("previousMonth", "2026-04")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("CARD_200_004"))
                .andExpect(jsonPath("$.message").value("전월 실적 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.baseMonth").value("2026-05"))
                .andExpect(jsonPath("$.data.rewardStatus").value("기준 충족"))
                .andExpect(jsonPath("$.data.previousMonthSpendAmount").value(820000))
                .andExpect(jsonPath("$.data.detail.totalSpendAmount").value(820000))
                .andExpect(jsonPath("$.data.detail.rewardPointAmount").value(8200));

        then(cardPerformanceService).should().getPreviousPerformance(USER_UUID, "2026-04");
    }

    @Test
    @DisplayName("실적 정보가 없으면 404 응답을 반환한다")
    void getPreviousPerformanceNotFound() throws Exception {
        // given
        given(cardPerformanceService.getPreviousPerformance(eq(USER_UUID), eq("2026-04")))
                .willThrow(new BusinessException(CardPerformanceErrorCode.PERFORMANCE_NOT_FOUND));

        // when & then
        mockMvc.perform(
                        get("/internal/cards/performance/monthly")
                                .header("X-User-UUID", USER_UUID.toString())
                                .param("previousMonth", "2026-04")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("CARD_404_001"))
                .andExpect(jsonPath("$.message").value("실적 정보를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("previousMonth 쿼리 파라미터 없이 전월 실적 조회 API를 호출한다")
    void getPreviousPerformanceWithoutPreviousMonth() throws Exception {
        // given
        PreviousPerformanceResponse response = new PreviousPerformanceResponse(
                "2026-05",
                "湲곗? 異⑹”",
                820000L,
                new PreviousPerformanceResponse.PerformanceDetail(820000L, 8200L)
        );

        given(cardPerformanceService.getPreviousPerformance(eq(USER_UUID), eq(null)))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/internal/cards/performance/monthly")
                                .header("X-User-UUID", USER_UUID.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("CARD_200_004"));

        then(cardPerformanceService).should().getPreviousPerformance(USER_UUID, null);
    }
}
