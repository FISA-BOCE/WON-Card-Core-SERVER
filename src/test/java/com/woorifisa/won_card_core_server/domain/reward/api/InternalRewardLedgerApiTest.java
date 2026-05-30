package com.woorifisa.won_card_core_server.domain.reward.api;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.woorifisa.won_card_core_server.domain.reward.dto.response.CurrentRewardsAmount;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardLedgerDetailResponse;
import com.woorifisa.won_card_core_server.domain.reward.dto.response.RewardLedgerResponse;
import com.woorifisa.won_card_core_server.domain.reward.service.RewardGetService;
import com.woorifisa.won_card_core_server.domain.reward.service.RewardLedgerService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InternalRewardLedgerApi.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalRewardLedgerApiTest {

    private static final UUID CARD_USER_UUID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID USER_UUID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RewardLedgerService rewardLedgerService;

    @MockitoBean
    private RewardGetService rewardGetService;

    @Test
    @DisplayName("내부 리워드 내역 조회 API를 호출한다")
    void getRewardLedger() throws Exception {
        // given
        RewardLedgerResponse response = new RewardLedgerResponse(
                2026,
                12450L,
                List.of(
                        new RewardLedgerResponse.RewardLedgerItem(
                                1001L,
                                "2026-05",
                                12450L,
                                "EARN",
                                LocalDateTime.of(2026, 5, 7, 14, 32)
                        )
                )
        );

        given(rewardLedgerService.getRewardLedger(eq(CARD_USER_UUID), eq("EARN")))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/internal/cards/rewards/ledger")
                                .param("type", "EARN")
                                .header("X-Card-User-UUID", CARD_USER_UUID.toString())
                                .header("X-Service-ID", "WON-CARD-CHANNEL")
                                .header("X-Transaction-ID", "TX-20260512-RWD02")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("리워드 내역 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.baseYear").value(2026))
                .andExpect(jsonPath("$.data.totalAccumulatedAmount").value(12450))
                .andExpect(jsonPath("$.data.ledgers[0].pointLedgerId").value(1001))
                .andExpect(jsonPath("$.data.ledgers[0].baseMonth").value("2026-05"))
                .andExpect(jsonPath("$.data.ledgers[0].pointAmount").value(12450))
                .andExpect(jsonPath("$.data.ledgers[0].type").value("EARN"));

        then(rewardLedgerService).should().getRewardLedger(CARD_USER_UUID, "EARN");
    }

    @Test
    @DisplayName("내부 리워드 내역 상세 조회 API를 호출한다")
    void getRewardLedgerDetail() throws Exception {
        // given
        Long pointLedgerId = 1L;

        RewardLedgerDetailResponse response = new RewardLedgerDetailResponse(
                pointLedgerId, "2026-05", "EARN",
                12450L, LocalDateTime.of(2026, 5, 7, 14, 32),
                new RewardLedgerDetailResponse.RewardDetail(820000L, 500000L, 0L)
        );

        given(rewardLedgerService.getRewardLedgerDetail(eq(CARD_USER_UUID), eq(pointLedgerId)))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/internal/cards/rewards/ledger/{pointLedgerId}", pointLedgerId)
                                .header("X-Card-User-UUID", CARD_USER_UUID.toString())
                                .header("X-Service-ID", "WON-CARD-CHANNEL")
                                .header("X-Transaction-ID", "TX-20260512-RWD03")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("상세 리워드 내역 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.pointLedgerId").value(1))
                .andExpect(jsonPath("$.data.baseMonth").value("2026-05"))
                .andExpect(jsonPath("$.data.type").value("EARN"))
                .andExpect(jsonPath("$.data.pointAmount").value(12450))
                .andExpect(jsonPath("$.data.detail.previousMonthSpendAmount").value(820000))
                .andExpect(jsonPath("$.data.detail.targetSpendAmount").value(500000))
                .andExpect(jsonPath("$.data.detail.shortfallAmount").value(0));
    }

    @Test
    @DisplayName("type 파라미터 없이 내부 리워드 내역을 조회한다")
    void getRewardLedgerWithoutType() throws Exception {
        // given
        RewardLedgerResponse response = new RewardLedgerResponse(
                2026,
                12450L,
                List.of()
        );

        given(rewardLedgerService.getRewardLedger(eq(CARD_USER_UUID), isNull()))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/internal/cards/rewards/ledger")
                                .header("X-Card-User-UUID", CARD_USER_UUID.toString())
                                .header("X-Service-ID", "WON-CARD-CHANNEL")
                                .header("X-Transaction-ID", "TX-20260512-RWD02")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("리워드 내역 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.baseYear").value(2026))
                .andExpect(jsonPath("$.data.totalAccumulatedAmount").value(12450))
                .andExpect(jsonPath("$.data.ledgers").isArray());

        then(rewardLedgerService).should().getRewardLedger(CARD_USER_UUID, null);
    }

    @Test
    @DisplayName("monthly reward API returns current reward amount")
    void getCurrentReward() throws Exception {
        // given
        CurrentRewardsAmount response = new CurrentRewardsAmount(
                "2026-05",
                "기준 충족",
                820000L,
                12450L,
                new BigDecimal("0.015"),
                "2"
        );

        given(rewardGetService.getCurrentReward(eq(USER_UUID)))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/internal/cards/rewards/monthly")
                                .header("X-User-UUID", USER_UUID.toString())
                                .header("X-Service-ID", "WON-CARD-CHANNEL")
                                .header("X-Transaction-ID", "TX-20260512-RWD04")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("CARD_200_004"))
                .andExpect(jsonPath("$.data.baseMonth").value("2026-05"))
                .andExpect(jsonPath("$.data.rewardStatus").value("기준 충족"))
                .andExpect(jsonPath("$.data.previousMonthSpendAmount").value(820000))
                .andExpect(jsonPath("$.data.rewardPointAmount").value(12450))
                .andExpect(jsonPath("$.data.rewardRate").value(0.015))
                .andExpect(jsonPath("$.data.performanceStatus").value("2"));

        then(rewardGetService).should().getCurrentReward(USER_UUID);
    }
}
