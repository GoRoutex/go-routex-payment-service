package vn.com.routex.hub.payment.service.infrastructure.kafka.event;

import lombok.Builder;
import vn.com.routex.hub.payment.service.domain.booking.PaymentStatus;

@Builder
public record PaymentFailedEvent(
        String paymentId,
        String bookingId,
        PaymentStatus status,
        String reason
) {
}
