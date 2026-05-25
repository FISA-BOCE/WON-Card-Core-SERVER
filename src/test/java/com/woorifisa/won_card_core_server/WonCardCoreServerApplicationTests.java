package com.woorifisa.won_card_core_server;

import com.woorifisa.won_card_core_server.domain.card.dto.request.CardApplicationRequest;
import com.woorifisa.won_card_core_server.domain.card.dto.response.CardApplicationResponse;
import com.woorifisa.won_card_core_server.domain.card.exception.CardErrorCode;
import com.woorifisa.won_card_core_server.domain.card.model.Gender;
import com.woorifisa.won_card_core_server.domain.card.repository.CardRepository;
import com.woorifisa.won_card_core_server.domain.card.repository.CardUserRepository;
import com.woorifisa.won_card_core_server.domain.card.service.CardApplicationService;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(properties = "app.security.crypto-secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")
class WonCardCoreServerApplicationTests {

    @Autowired
    private CardApplicationService cardApplicationService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardUserRepository cardUserRepository;

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
        cardUserRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void createCardApplication_createsCardAndCardUser() {
        CardApplicationRequest request = createRequest(UUID.randomUUID().toString(), true);

        CardApplicationResponse response = cardApplicationService.createCardApplication(request);

        assertThat(response.cardUuid()).isNotBlank();
        assertThat(response.cardNoDisplay()).startsWith("****-****-****-");
        assertThat(response.cardStatus()).isEqualTo("ACTIVE");
        assertThat(cardUserRepository.findByUserUuid(request.userUuid())).isPresent();
        assertThat(cardRepository.count()).isEqualTo(1);
    }

    @Test
    void createCardApplication_whenRequiredTermsNotAgreed_throwsBadRequestAndDoesNotCreateCardUser() {
        CardApplicationRequest request = createRequest(UUID.randomUUID().toString(), false);

        assertThatThrownBy(() -> cardApplicationService.createCardApplication(request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CardErrorCode.REQUIRED_TERMS_NOT_AGREED));

        assertThat(cardUserRepository.findByUserUuid(request.userUuid())).isEmpty();
        assertThat(cardRepository.count()).isZero();
    }

    @Test
    void createCardApplication_whenCardAlreadyExists_throwsConflict() {
        CardApplicationRequest request = createRequest(UUID.randomUUID().toString(), true);
        cardApplicationService.createCardApplication(request);

        assertThatThrownBy(() -> cardApplicationService.createCardApplication(request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CardErrorCode.CARD_ALREADY_EXISTS));

        assertThat(cardRepository.count()).isEqualTo(1);
    }

    @Test
    void createCardApplication_whenCiHashAlreadyExists_throwsConflict() {
        CardApplicationRequest firstRequest = createRequest(UUID.randomUUID().toString(), true);
        CardApplicationRequest secondRequest = createRequest(UUID.randomUUID().toString(), true);
        cardApplicationService.createCardApplication(firstRequest);

        assertThatThrownBy(() -> cardApplicationService.createCardApplication(secondRequest))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CardErrorCode.CARD_USER_ALREADY_EXISTS));

        assertThat(cardUserRepository.count()).isEqualTo(1);
        assertThat(cardRepository.count()).isEqualTo(1);
    }

    private CardApplicationRequest createRequest(String userUuid, boolean isAgree) {
        return new CardApplicationRequest(
                userUuid,
                "userNameEnc",
                "birthDateEnc",
                Gender.M,
                "KR",
                isAgree,
                "telEnc",
                "emailEnc",
                "addressEnc"
        );
    }
}
