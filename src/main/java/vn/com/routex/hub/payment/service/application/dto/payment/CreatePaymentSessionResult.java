package vn.com.routex.hub.payment.service.application.dto.payment;

import lombok.Builder;
import vn.com.routex.hub.payment.service.domain.booking.PaymentStatus;
import vn.com.routex.hub.payment.service.interfaces.models.result.ApiResult;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record CreatePaymentSessionResult(
        ApiResult result,
        String paymentId,
        String bookingId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String qrContent,
        String checkoutUrl,
        OffsetDateTime expiresAt
) {
}
