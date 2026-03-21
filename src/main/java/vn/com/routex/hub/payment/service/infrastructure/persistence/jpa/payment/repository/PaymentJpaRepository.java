package vn.com.routex.hub.payment.service.infrastructure.persistence.jpa.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.routex.hub.payment.service.domain.booking.PaymentStatus;
import vn.com.routex.hub.payment.service.infrastructure.persistence.jpa.payment.entity.PaymentJpaEntity;

import java.util.Optional;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, String> {
    Optional<PaymentJpaEntity> findByBookingIdAndStatus(String bookingId, PaymentStatus status);
}
