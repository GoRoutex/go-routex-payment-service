package vn.com.routex.hub.payment.service.infrastructure.persistence.jpa.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.routex.hub.payment.service.domain.booking.PaymentStatus;
import vn.com.routex.hub.payment.service.domain.payment.PaymentMethod;
import vn.com.routex.hub.payment.service.domain.payment.model.PaymentAggregate;
import vn.com.routex.hub.payment.service.infrastructure.persistence.jpa.payment.entity.PaymentEntity;

import java.util.Optional;

@Repository
public interface PaymentEntityRepository extends JpaRepository<PaymentEntity, String> {
    Optional<PaymentEntity> findByBookingCodeAndMethodAndStatus(String bookingCode, PaymentMethod method, PaymentStatus status);

    Optional<PaymentEntity> findByTxnRef(String txnRef);
}
