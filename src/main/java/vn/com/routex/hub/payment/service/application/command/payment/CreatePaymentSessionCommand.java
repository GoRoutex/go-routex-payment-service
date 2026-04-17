package vn.com.routex.hub.payment.service.application.command.payment;

import lombok.Builder;

@Builder
public record CreatePaymentSessionCommand(
        RequestMetadata metadata,
        String bookingId,
        String customerId
) {
}
