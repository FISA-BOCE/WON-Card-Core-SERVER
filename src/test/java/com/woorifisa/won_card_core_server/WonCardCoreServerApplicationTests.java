package com.woorifisa.won_card_core_server;

import com.woorifisa.won_card_core_server.domain.card.dto.request.CardApplicationRequest;
import com.woorifisa.won_card_core_server.domain.card.dto.response.CardApplicationResponse;
import com.woorifisa.won_card_core_server.domain.card.exception.code.CardErrorCode;
import com.woorifisa.won_card_core_server.domain.card.model.CardUser;
import com.woorifisa.won_card_core_server.domain.card.model.CardUserStatus;
import com.woorifisa.won_card_core_server.domain.card.model.Gender;
import com.woorifisa.won_card_core_server.domain.card.repository.CardRepository;
import com.woorifisa.won_card_core_server.domain.card.repository.CardUserRepository;
import com.woorifisa.won_card_core_server.domain.card.service.CardApplicationService;
import com.woorifisa.won_card_core_server.domain.performance.repository.CardPerformanceRepository;
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
    private CardPerformanceRepository cardPerformanceRepository;

    @Autowired
    private TextEncryptor textEncryptor;

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
        cardPerformanceRepository.deleteAll();
        cardUserRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void createCardApplication_createsCardAndCardUser() {
        UUID userUuid = UUID.randomUUID();
        CardApplicationRequest request = createRequest(true);

        CardApplicationResponse response = cardApplicationService.createCardApplication(userUuid, request);

        assertThat(response.cardUserUuid()).isNotNull();
        assertThat(response.cardUuid()).isNotNull();
        assertThat(response.cardNoDisplay()).startsWith("****-****-****-");
        assertThat(response.issuedAt()).isNotNull();
        assertThat(response.cardStatus()).isEqualTo("ACTIVE");
        CardUser savedCardUser = cardUserRepository.findByUserUuid(userUuid).orElseThrow();
        assertThat(response.cardUserUuid()).isEqualTo(savedCardUser.getCardUserUuid());
        assertThat(cardRepository.count()).isEqualTo(1);
        assertThat(cardPerformanceRepository.count()).isEqualTo(1);
    }

    @Test
    void createCardApplication_whenCardUserAlreadyExists_reusesCardUserUuidInResponse() {
        UUID userUuid = UUID.randomUUID();
        CardUser existingCardUser = cardUserRepository.saveAndFlush(CardUser.builder()
                .userUuid(userUuid)
                .userNameEnc(textEncryptor.encrypt("Existing User"))
                .birthDateEnc(textEncryptor.encrypt("19900101"))
                .gender(Gender.M)
                .ciHash("existing-ci-hash")
                .nationality("KR")
                .userStatus(CardUserStatus.ACTIVE)
                .isAgree(true)
                .telEnc(textEncryptor.encrypt("01099998888"))
                .emailEnc(textEncryptor.encrypt("existing@example.com"))
                .addressEnc(textEncryptor.encrypt("Seoul Mapo-gu"))
                .build());
        CardApplicationRequest request = createRequest(true);

        CardApplicationResponse response = cardApplicationService.createCardApplication(userUuid, request);

        assertThat(response.cardUserUuid()).isEqualTo(existingCardUser.getCardUserUuid());
        assertThat(response.issuedAt()).isNotNull();
        assertThat(cardUserRepository.count()).isEqualTo(1);
        assertThat(cardRepository.count()).isEqualTo(1);
        assertThat(cardPerformanceRepository.count()).isEqualTo(1);
    }

    @Test
    void createCardApplication_whenRequiredTermsNotAgreed_throwsBadRequestAndDoesNotCreateCardUser() {
        UUID userUuid = UUID.randomUUID();
        CardApplicationRequest request = createRequest(false);

        assertThatThrownBy(() -> cardApplicationService.createCardApplication(userUuid, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CardErrorCode.REQUIRED_TERMS_NOT_AGREED));

        assertThat(cardUserRepository.findByUserUuid(userUuid)).isEmpty();
        assertThat(cardRepository.count()).isZero();
        assertThat(cardPerformanceRepository.count()).isZero();
    }

    @Test
    void createCardApplication_whenCardAlreadyExists_throwsConflict() {
        UUID userUuid = UUID.randomUUID();
        CardApplicationRequest request = createRequest(true);
        cardApplicationService.createCardApplication(userUuid, request);

        assertThatThrownBy(() -> cardApplicationService.createCardApplication(userUuid, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CardErrorCode.CARD_ALREADY_EXISTS));

        assertThat(cardRepository.count()).isEqualTo(1);
        assertThat(cardPerformanceRepository.count()).isEqualTo(1);
    }

    @Test
    void createCardApplication_whenCiHashAlreadyExists_throwsConflict() {
        CardApplicationRequest firstRequest = createRequest(true);
        CardApplicationRequest secondRequest = createRequest(true);
        cardApplicationService.createCardApplication(UUID.randomUUID(), firstRequest);

        assertThatThrownBy(() -> cardApplicationService.createCardApplication(UUID.randomUUID(), secondRequest))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CardErrorCode.CARD_USER_ALREADY_EXISTS));

        assertThat(cardUserRepository.count()).isEqualTo(1);
        assertThat(cardRepository.count()).isEqualTo(1);
        assertThat(cardPerformanceRepository.count()).isEqualTo(1);
    }

    @Test
    void createCardApplication_whenEncryptedValueInvalid_throwsBadRequest() {
        CardApplicationRequest request = new CardApplicationRequest(
                "invalid-encrypted-value",
                textEncryptor.encrypt("19900101"),
                Gender.M,
                "KR",
                true,
                textEncryptor.encrypt("01012345678"),
                textEncryptor.encrypt("test@example.com"),
                textEncryptor.encrypt("Seoul Jung-gu")
        );

        assertThatThrownBy(() -> cardApplicationService.createCardApplication(UUID.randomUUID(), request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CardErrorCode.INVALID_ENCRYPTED_VALUE));

        assertThat(cardUserRepository.count()).isZero();
        assertThat(cardRepository.count()).isZero();
        assertThat(cardPerformanceRepository.count()).isZero();
    }

    @Test
    void printEncryptedValuesForPostman() {
        System.out.println("userNameEnc = " + textEncryptor.encrypt("Hong Gil Dong"));
        System.out.println("birthDateEnc = " + textEncryptor.encrypt("19900101"));
        System.out.println("telEnc = " + textEncryptor.encrypt("01012345678"));
        System.out.println("emailEnc = " + textEncryptor.encrypt("test@example.com"));
        System.out.println("addressEnc = " + textEncryptor.encrypt("Seoul Jung-gu"));
    }

    private CardApplicationRequest createRequest(boolean isAgree) {
        return new CardApplicationRequest(
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
