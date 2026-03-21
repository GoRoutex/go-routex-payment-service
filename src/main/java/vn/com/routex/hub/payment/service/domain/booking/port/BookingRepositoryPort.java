package vn.com.routex.hub.payment.service.domain.booking.port;

import vn.com.routex.hub.payment.service.domain.booking.model.BookingAggregate;

import java.util.Optional;

public interface BookingRepositoryPort {
    Optional<BookingAggregate> findById(String bookingId);
}
