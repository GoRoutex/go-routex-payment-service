package vn.com.routex.hub.payment.service.infrastructure.persistence.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import vn.com.go.routex.identity.security.jwt.JwtService;
import vn.com.go.routex.identity.security.log.SystemLog;
import vn.com.routex.hub.payment.service.infrastructure.persistence.config.RequestAttributes;
import vn.com.routex.hub.payment.service.infrastructure.persistence.security.envelope.RequestEnvelopeExtractor;
import vn.com.routex.hub.payment.service.interfaces.model.base.BaseRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final JwtService jwtService;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        if (shouldBypassEnvelope(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        CachedHttpServletRequestWrapper cachedHttpServletRequestWrapper = new CachedHttpServletRequestWrapper(request);
        ContentCachingResponseWrapper contentCachingResponseWrapper = new ContentCachingResponseWrapper(response);

        try {
            String jsonStringBody = new String(
                    cachedHttpServletRequestWrapper.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );
            BaseRequest apiRequest = RequestEnvelopeExtractor.extract(request, jsonStringBody, objectMapper);
            request.setAttribute(RequestAttributes.REQUEST_ID, apiRequest.getRequestId());
            request.setAttribute(RequestAttributes.REQUEST_DATE_TIME, apiRequest.getRequestDateTime());
            request.setAttribute(RequestAttributes.CHANNEL, apiRequest.getChannel());
            String merchantId = extractMerchantIdFromJwt(request);
            if (merchantId != null && !merchantId.isBlank()) {
                request.setAttribute(RequestAttributes.MERCHANT_ID, merchantId.trim());
            }
        } catch (JsonProcessingException | IllegalArgumentException e) {
            // Invalid envelope request (requestId/requestDateTime/channel missing/invalid JSON).
            sLog.warn("Invalid request envelope: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid Request");
            response.getWriter().flush();
            return;
        }

        try {
            filterChain.doFilter(cachedHttpServletRequestWrapper, contentCachingResponseWrapper);
        } finally {
            // Always mirror the response back to the client and log it.
            String responseMessage = new String(contentCachingResponseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            sLog.info("{}", responseMessage);
            contentCachingResponseWrapper.copyBodyToResponse();
        }
    }

    private String extractMerchantIdFromJwt(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }

        try {
            String token = authorization.substring(7);
            var claims = jwtService.extractAllClaims(token);

            String merchantId = claims.get("merchantId", String.class);
            if (merchantId != null && !merchantId.isBlank()) {
                return merchantId;
            }

            Object fallback = claims.get("merchant_id");
            return fallback == null ? null : fallback.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean shouldBypassEnvelope(String requestURI) {
        return requestURI.startsWith("/actuator/")
                || requestURI.contains("/location-service/")
                || requestURI.startsWith("/swagger-ui")
                || requestURI.startsWith("/v3/api-docs")
                || requestURI.equals("/api/v1/payment-service/vnpay-ipn");
    }
}
