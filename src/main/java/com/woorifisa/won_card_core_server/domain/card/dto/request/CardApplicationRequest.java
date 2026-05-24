package com.woorifisa.won_card_core_server.domain.card.dto.request;

import com.woorifisa.won_card_core_server.domain.card.model.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CardApplicationRequest(
        @NotBlank
        @Size(max = 36)
        String userUuid,

        @NotBlank
        String userNameEnc,

        @NotBlank
        String birthDateEnc,

        @NotNull
        Gender gender,

        @NotBlank
        @Size(max = 30)
        String nationality,

        @NotNull
        Boolean isAgree,

        @NotBlank
        String telEnc,

        @NotBlank
        String emailEnc,

        @NotBlank
        @Size(max = 1024)
        String addressEnc
) {
}
