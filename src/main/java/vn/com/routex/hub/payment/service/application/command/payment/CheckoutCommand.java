package vn.com.routex.hub.payment.service.application.command.payment;

import lombok.Builder;
import vn.com.routex.hub.payment.service.application.command.common.RequestContext;

@Builder
public record CheckoutCommand(
        RequestContext context,
        String paymentId,
        String token
) {
}
