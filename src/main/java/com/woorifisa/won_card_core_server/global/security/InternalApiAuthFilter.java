package com.woorifisa.won_card_core_server.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorifisa.won_card_core_server.global.exception.code.CommonErrorCode;
import com.woorifisa.won_card_core_server.global.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Slf4j
@Component
public class InternalApiAuthFilter extends OncePerRequestFilter {

    private static final String SERVICE_ID_HEADER = "X-Service-ID";
    private static final String API_KEY_HEADER = "X-Internal-Api-Key";

    private final String expectedServiceId;
    private final String expectedApiKey;
    private final ObjectMapper objectMapper;

    public InternalApiAuthFilter(
            ObjectMapper objectMapper,
            @Value("${internal.auth.service-id:}") String expectedServiceId,
            @Value("${internal.auth.api-key:}") String expectedApiKey
    ) {
        this.objectMapper = objectMapper;
        this.expectedServiceId = normalize(expectedServiceId);
        this.expectedApiKey = normalize(expectedApiKey);
        validateInternalAuthProperties();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = resolveRequestPath(request);
        return !(path.equals("/internal") || path.startsWith("/internal/"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String serviceId = request.getHeader(SERVICE_ID_HEADER);
        String apiKey = request.getHeader(API_KEY_HEADER);

        if (!isValidInternalRequest(serviceId, apiKey)) {
            writeErrorResponse(response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        serviceId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private String resolveRequestPath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (servletPath != null && !servletPath.isBlank()) {
            return servletPath;
        }

        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null
                && !contextPath.isBlank()
                && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }

        return requestUri;
    }

    private boolean isValidInternalRequest(String serviceId, String apiKey) {
        if (!hasText(expectedServiceId) || !hasText(expectedApiKey)) {
            log.error("내부 API 인증 설정이 누락되었습니다.");
            return false;
        }

        return constantTimeEquals(expectedServiceId, serviceId)
                && constantTimeEquals(expectedApiKey, apiKey);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private void validateInternalAuthProperties() {
        if (!hasText(expectedServiceId) || !hasText(expectedApiKey)) {
            log.error("internal.auth.service-id and internal.auth.api-key must not be blank.");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }

        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = actual.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(expectedBytes, actualBytes);
    }

    private void writeErrorResponse(HttpServletResponse response) throws IOException {
        response.setStatus(CommonErrorCode.UNAUTHORIZED.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), ErrorResponse.of(CommonErrorCode.UNAUTHORIZED));
    }
}
