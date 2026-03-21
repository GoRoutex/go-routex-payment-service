package vn.com.routex.hub.payment.service.infrastructure.persistence.adapter.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.com.routex.hub.payment.service.domain.booking.PaymentStatus;
import vn.com.routex.hub.payment.service.domain.payment.model.PaymentAggregate;
import vn.com.routex.hub.payment.service.domain.payment.port.PaymentRepositoryPort;
import vn.com.routex.hub.payment.service.infrastructure.persistence.jpa.payment.repository.PaymentJpaRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentPersistenceMapper paymentPersistenceMapper;

    @Override
    public Optional<PaymentAggregate> findById(String paymentId) {
        return paymentJpaRepository.findById(paymentId).map(paymentPersistenceMapper::toDomain);
    }

    @Override
    public Optional<PaymentAggregate> findByBookingIdAndStatus(String bookingId, PaymentStatus status) {
        return paymentJpaRepository.findByBookingIdAndStatus(bookingId, status)
                .map(paymentPersistenceMapper::toDomain);
    }

    @Override
    public PaymentAggregate save(PaymentAggregate paymentAggregate) {
        return paymentPersistenceMapper.toDomain(
                paymentJpaRepository.save(paymentPersistenceMapper.toJpaEntity(paymentAggregate))
        );
    }
}
