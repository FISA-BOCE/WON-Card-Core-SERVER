package com.woorifisa.won_card_core_server;

import com.woorifisa.won_card_core_server.domain.card.dto.request.CardApplicationRequest;
import com.woorifisa.won_card_core_server.domain.card.dto.response.CardApplicationResponse;
import com.woorifisa.won_card_core_server.domain.card.exception.CardErrorCode;
import com.woorifisa.won_card_core_server.domain.card.model.Gender;
import com.woorifisa.won_card_core_server.domain.card.repository.CardRepository;
import com.woorifisa.won_card_core_server.domain.card.repository.CardUserRepository;
import com.woorifisa.won_card_core_server.domain.card.service.CardApplicationService;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import com.woorifisa.won_card_core_server.global.security.TextEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
class WonCardCoreServerApplicationTests {

    @Autowired
    private CardApplicationService cardApplicationService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardUserRepository cardUserRepository;

    @Autowired
    private TextEncryptor textEncryptor;

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
        CardApplicationRequest request = createRequest(UUID.randomUUID(), true);

        CardApplicationResponse response = cardApplicationService.createCardApplication(request);

        assertThat(response.cardUuid()).isNotNull();
        assertThat(response.cardNoDisplay()).startsWith("****-****-****-");
        assertThat(response.cardStatus()).isEqualTo("ACTIVE");
        assertThat(cardUserRepository.findByUserUuid(request.userUuid())).isPresent();
        assertThat(cardRepository.count()).isEqualTo(1);
    }

    @Test
    void createCardApplication_whenRequiredTermsNotAgreed_throwsBadRequestAndDoesNotCreateCardUser() {
        CardApplicationRequest request = createRequest(UUID.randomUUID(), false);

        assertThatThrownBy(() -> cardApplicationService.createCardApplication(request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CardErrorCode.REQUIRED_TERMS_NOT_AGREED));

        assertThat(cardUserRepository.findByUserUuid(request.userUuid())).isEmpty();
        assertThat(cardRepository.count()).isZero();
    }

    @Test
    void createCardApplication_whenCardAlreadyExists_throwsConflict() {
        CardApplicationRequest request = createRequest(UUID.randomUUID(), true);
        cardApplicationService.createCardApplication(request);

        assertThatThrownBy(() -> cardApplicationService.createCardApplication(request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CardErrorCode.CARD_ALREADY_EXISTS));

        assertThat(cardRepository.count()).isEqualTo(1);
    }

    @Test
    void createCardApplication_whenCiHashAlreadyExists_throwsConflict() {
        CardApplicationRequest firstRequest = createRequest(UUID.randomUUID(), true);
        CardApplicationRequest secondRequest = createRequest(UUID.randomUUID(), true);
        cardApplicationService.createCardApplication(firstRequest);

        assertThatThrownBy(() -> cardApplicationService.createCardApplication(secondRequest))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CardErrorCode.CARD_USER_ALREADY_EXISTS));

        assertThat(cardUserRepository.count()).isEqualTo(1);
        assertThat(cardRepository.count()).isEqualTo(1);
    }

    @Test
    void createCardApplication_whenEncryptedValueInvalid_throwsBadRequest() {
        CardApplicationRequest request = new CardApplicationRequest(
                UUID.randomUUID(),
                "invalid-encrypted-value",
                textEncryptor.encrypt("19900101"),
                Gender.M,
                "KR",
                true,
                textEncryptor.encrypt("01012345678"),
                textEncryptor.encrypt("test@example.com"),
                textEncryptor.encrypt("Seoul Jung-gu")
        );

        assertThatThrownBy(() -> cardApplicationService.createCardApplication(request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CardErrorCode.INVALID_ENCRYPTED_VALUE));

        assertThat(cardUserRepository.count()).isZero();
        assertThat(cardRepository.count()).isZero();
    }

    @Test
    void printEncryptedValuesForPostman() {
        System.out.println("userNameEnc = " + textEncryptor.encrypt("Hong Gil Dong"));
        System.out.println("birthDateEnc = " + textEncryptor.encrypt("19900101"));
        System.out.println("telEnc = " + textEncryptor.encrypt("01012345678"));
        System.out.println("emailEnc = " + textEncryptor.encrypt("test@example.com"));
        System.out.println("addressEnc = " + textEncryptor.encrypt("Seoul Jung-gu"));
    }

    private CardApplicationRequest createRequest(UUID userUuid, boolean isAgree) {
        return new CardApplicationRequest(
                userUuid,
                textEncryptor.encrypt("Hong Gil Dong"),
                textEncryptor.encrypt("19900101"),
                Gender.M,
                "KR",
                isAgree,
                textEncryptor.encrypt("01012345678"),
                textEncryptor.encrypt("test@example.com"),
                textEncryptor.encrypt("Seoul Jung-gu")
        );
    }
}
