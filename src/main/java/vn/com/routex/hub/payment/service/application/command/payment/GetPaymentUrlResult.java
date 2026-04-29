package vn.com.routex.hub.payment.service.application.command.payment;

import lombok.Builder;
import vn.com.routex.hub.payment.service.domain.payment.PaymentMethod;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record GetPaymentUrlResult(
        String bookingCode,
        BigDecimal amount,
        PaymentMethod method,
        String qrCodeUrl,
        String paymentUrl,
        String deeplink,
        OffsetDateTime expiredTime
) {
}
