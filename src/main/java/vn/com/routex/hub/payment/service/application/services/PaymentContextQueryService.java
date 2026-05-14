package vn.com.routex.hub.payment.service.application.services;

import vn.com.routex.hub.payment.service.application.command.common.RequestContext;
import vn.com.routex.hub.payment.service.domain.payment.model.PaymentAggregate;

public interface PaymentContextQueryService {

    PaymentAggregate getPaymentContext(String bookingCode, RequestContext context);
}
