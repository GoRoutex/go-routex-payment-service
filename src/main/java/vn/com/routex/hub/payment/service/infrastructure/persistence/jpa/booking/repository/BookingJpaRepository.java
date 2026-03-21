package vn.com.routex.hub.payment.service.infrastructure.persistence.jpa.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.routex.hub.payment.service.infrastructure.persistence.jpa.booking.entity.BookingJpaEntity;

@Repository
public interface BookingJpaRepository extends JpaRepository<BookingJpaEntity, String> {
}
