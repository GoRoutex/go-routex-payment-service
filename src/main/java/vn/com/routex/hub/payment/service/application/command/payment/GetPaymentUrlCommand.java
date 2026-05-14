package vn.com.routex.hub.payment.service.application.command.payment;

import lombok.Builder;
import vn.com.routex.hub.payment.service.application.command.common.RequestContext;
import vn.com.routex.hub.payment.service.domain.payment.PaymentMethod;

import java.math.BigDecimal;

@Builder
public record GetPaymentUrlCommand(
        RequestContext context,
        BigDecimal amount,
        String referenceNo,
        String bankCode,
        String clientIp,
        String bookingCode,
        PaymentMethod method
) {
}
