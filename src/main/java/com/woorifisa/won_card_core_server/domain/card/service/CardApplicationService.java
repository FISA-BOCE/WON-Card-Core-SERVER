package com.woorifisa.won_card_core_server.domain.card.service;

import com.woorifisa.won_card_core_server.domain.card.dto.request.CardApplicationRequest;
import com.woorifisa.won_card_core_server.domain.card.dto.response.CardApplicationResponse;
import com.woorifisa.won_card_core_server.domain.card.exception.code.CardErrorCode;
import com.woorifisa.won_card_core_server.domain.card.model.Card;
import com.woorifisa.won_card_core_server.domain.card.model.CardStatus;
import com.woorifisa.won_card_core_server.domain.card.model.CardUser;
import com.woorifisa.won_card_core_server.domain.card.model.CardUserStatus;
import com.woorifisa.won_card_core_server.domain.card.repository.CardRepository;
import com.woorifisa.won_card_core_server.domain.card.repository.CardUserRepository;
import com.woorifisa.won_card_core_server.domain.performance.model.CardPerformance;
import com.woorifisa.won_card_core_server.domain.performance.repository.CardPerformanceRepository;
import com.woorifisa.won_card_core_server.global.exception.handler.BusinessException;
import com.woorifisa.won_card_core_server.global.security.TextEncryptor;
import com.woorifisa.won_card_core_server.global.util.HashUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardApplicationService {

    private static final int CARD_NUMBER_LENGTH = 16;
    private static final int CARD_VALID_YEARS = 5;
    private static final BigDecimal DEFAULT_LIMIT_AMOUNT = BigDecimal.ZERO;
    private static final BigDecimal DEFAULT_PERFORMANCE_AMOUNT = BigDecimal.ZERO;

    private final CardUserRepository cardUserRepository;
    private final CardRepository cardRepository;
    private final CardPerformanceRepository cardPerformanceRepository;
    private final TextEncryptor textEncryptor;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public CardApplicationResponse createCardApplication(UUID userUuid, CardApplicationRequest request) {
        validateAgreement(request);

        CardUser cardUser = cardUserRepository.findByUserUuid(userUuid)
                .map(this::issueForExistingUser)
                .orElseGet(() -> createCardUser(userUuid, request));

        Card card = createCard(cardUser);
        Card savedCard = saveCard(card);
        saveCardPerformance(createCardPerformance(userUuid, cardUser));

        return CardApplicationResponse.from(savedCard);
    }

    private CardUser issueForExistingUser(CardUser cardUser) {
        if (cardUser.getUserStatus() != CardUserStatus.ACTIVE) {
            throw new BusinessException(CardErrorCode.CARD_ISSUANCE_NOT_ALLOWED);
        }
        if (cardRepository.existsByCardUserUuid(cardUser.getCardUserUuid())) {
            throw new BusinessException(CardErrorCode.CARD_ALREADY_EXISTS);
        }
        return cardUser;
    }

    private CardUser createCardUser(UUID userUuid, CardApplicationRequest request) {
        String ciHash = createCiHash(request);
        if (cardUserRepository.existsByCiHash(ciHash)) {
            throw new BusinessException(CardErrorCode.CARD_USER_ALREADY_EXISTS);
        }

        CardUser cardUser = CardUser.builder()
                .userUuid(userUuid)
                .userNameEnc(request.userNameEnc())
                .birthDateEnc(request.birthDateEnc())
                .gender(request.gender())
                .ciHash(ciHash)
                .nationality(request.nationality())
                .userStatus(CardUserStatus.ACTIVE)
                .isAgree(request.isAgree())
                .telEnc(request.telEnc())
                .emailEnc(request.emailEnc())
                .addressEnc(request.addressEnc())
                .build();

        try {
            return cardUserRepository.saveAndFlush(cardUser);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(CardErrorCode.CARD_USER_CONSTRAINT_CONFLICT);
        }
    }

    private void validateAgreement(CardApplicationRequest request) {
        if (!Boolean.TRUE.equals(request.isAgree())) {
            throw new BusinessException(CardErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }
    }

    private Card createCard(CardUser cardUser) {
        LocalDateTime issuedAt = LocalDateTime.now();
        String cardNo = generateUniqueCardNo();

        return Card.builder()
                .cardUser(cardUser)
                .cardNoToken(HashUtils.sha256(cardNo))
                .cardNoDisplay(maskCardNo(cardNo))
                .cardStatus(CardStatus.ACTIVE)
                .issuedAt(issuedAt)
                .expiredAt(issuedAt.plusYears(CARD_VALID_YEARS))
                .totalLimitAmount(DEFAULT_LIMIT_AMOUNT)
                .availableLimitAmount(DEFAULT_LIMIT_AMOUNT)
                .build();
    }

    private Card saveCard(Card card) {
        try {
            return cardRepository.saveAndFlush(card);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(CardErrorCode.CARD_CONSTRAINT_CONFLICT);
        }
    }

    private CardPerformance createCardPerformance(UUID userUuid, CardUser cardUser) {
        return CardPerformance.builder()
                .userUuid(userUuid)
                .cardUserUuid(cardUser.getCardUserUuid())
                .cardUser(cardUser)
                .baseMonth(YearMonth.now().toString())
                .previousMonthSpendAmount(DEFAULT_PERFORMANCE_AMOUNT)
                .currentMonthSpendAmount(DEFAULT_PERFORMANCE_AMOUNT)
                .rewardRate(DEFAULT_PERFORMANCE_AMOUNT)
                .rewardPointAmount(DEFAULT_PERFORMANCE_AMOUNT)
                .performanceStatus(null)
                .calculatedAt(null)
                .confirmedAt(null)
                .build();
    }

    private void saveCardPerformance(CardPerformance cardPerformance) {
        try {
            cardPerformanceRepository.saveAndFlush(cardPerformance);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(CardErrorCode.CARD_PERFORMANCE_CONSTRAINT_CONFLICT);
        }
    }

    private String generateUniqueCardNo() {
        String cardNo;
        do {
            cardNo = generateCardNo();
        } while (cardRepository.existsByCardNoToken(HashUtils.sha256(cardNo)));
        return cardNo;
    }

    private String generateCardNo() {
        StringBuilder builder = new StringBuilder(CARD_NUMBER_LENGTH);
        for (int i = 0; i < CARD_NUMBER_LENGTH; i++) {
            builder.append(secureRandom.nextInt(10));
        }
        return builder.toString();
    }

    private String maskCardNo(String cardNo) {
        return "****-****-****-" + cardNo.substring(cardNo.length() - 4);
    }

    private String createCiHash(CardApplicationRequest request) {
        String source = String.join("|",
                decryptRequired(request.userNameEnc()),
                decryptRequired(request.birthDateEnc()),
                request.gender().name(),
                decryptRequired(request.telEnc())
        );
        return HashUtils.sha256(source);
    }

    private String decryptRequired(String encryptedValue) {
        try {
            return textEncryptor.decrypt(encryptedValue);
        } catch (RuntimeException e) {
            throw new BusinessException(CardErrorCode.INVALID_ENCRYPTED_VALUE);
        }
    }
}
