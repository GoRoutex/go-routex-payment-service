package vn.com.routex.hub.payment.service.infrastructure.persistence.adapter.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.com.routex.hub.payment.service.domain.booking.model.BookingAggregate;
import vn.com.routex.hub.payment.service.domain.booking.port.BookingRepositoryPort;
import vn.com.routex.hub.payment.service.infrastructure.persistence.jpa.booking.repository.BookingJpaRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BookingRepositoryAdapter implements BookingRepositoryPort {

    private final BookingJpaRepository bookingJpaRepository;
    private final BookingPersistenceMapper bookingPersistenceMapper;

    @Override
    public Optional<BookingAggregate> findById(String bookingId) {
        return bookingJpaRepository.findById(bookingId).map(bookingPersistenceMapper::toDomain);
    }
}
