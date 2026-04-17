package vn.com.routex.hub.payment.service.domain.payment.port;

import vn.com.routex.hub.payment.service.application.command.payment.RequestMetadata;
import vn.com.routex.hub.payment.service.domain.payment.model.PaymentAggregate;

public interface PaymentEventPublisherPort {
    void publishPaymentSucceeded(RequestMetadata metadata, PaymentAggregate paymentAggregate);

    void publishPaymentFailed(RequestMetadata metadata, PaymentAggregate paymentAggregate, String reason);
}
