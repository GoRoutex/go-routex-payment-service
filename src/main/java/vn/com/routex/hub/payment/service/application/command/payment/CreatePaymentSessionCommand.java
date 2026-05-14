package vn.com.routex.hub.payment.service.application.command.payment;

import lombok.Builder;
import vn.com.routex.hub.payment.service.application.command.common.RequestContext;

@Builder
public record CreatePaymentSessionCommand(
        RequestContext context,
        String bookingId,
        String customerId
) {
}
