package vn.com.routex.hub.payment.service.application.command.payment;

import lombok.Builder;

@Builder
public record CheckoutCommand(
        RequestMetadata metadata,
        String paymentId,
        String token
) {
}
