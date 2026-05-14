package vn.com.routex.hub.payment.service.application.command.payment;

import lombok.Builder;
import vn.com.routex.hub.payment.service.domain.booking.PaymentStatus;

import java.math.BigDecimal;

@Builder
public record PollingPaymentStatusResult(
        String bookingCode,
        BigDecimal amount,
        PaymentStatus status,
        boolean shouldStopPooling
) {
}
