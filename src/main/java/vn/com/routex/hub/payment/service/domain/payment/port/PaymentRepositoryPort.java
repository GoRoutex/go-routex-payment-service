package vn.com.routex.hub.payment.service.domain.payment.port;

import vn.com.routex.hub.payment.service.domain.booking.PaymentStatus;
import vn.com.routex.hub.payment.service.domain.payment.model.PaymentAggregate;

import java.util.Optional;

public interface PaymentRepositoryPort {
    Optional<PaymentAggregate> findById(String paymentId);

    Optional<PaymentAggregate> findByBookingIdAndStatus(String bookingId, PaymentStatus status);

    PaymentAggregate save(PaymentAggregate paymentAggregate);
}
