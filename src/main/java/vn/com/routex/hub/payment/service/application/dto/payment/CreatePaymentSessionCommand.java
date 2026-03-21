package vn.com.routex.hub.payment.service.application.dto.payment;

import lombok.Builder;

@Builder
public record CreatePaymentSessionCommand(
        RequestMetadata metadata,
        String bookingId,
        String customerId
) {
}
