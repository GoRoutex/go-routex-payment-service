package vn.com.routex.hub.payment.service.application.command.payment;

import lombok.Builder;
import vn.com.routex.hub.payment.service.domain.booking.PaymentStatus;
import vn.com.routex.hub.payment.service.interfaces.model.result.ApiResult;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record CheckoutResult(
        ApiResult result,
        String paymentId,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        OffsetDateTime paidAt
) {
}
