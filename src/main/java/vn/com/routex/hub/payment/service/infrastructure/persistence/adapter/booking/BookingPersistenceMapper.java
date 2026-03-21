package vn.com.routex.hub.payment.service.infrastructure.persistence.adapter.booking;

import org.springframework.stereotype.Component;
import vn.com.routex.hub.payment.service.domain.booking.model.BookingAggregate;
import vn.com.routex.hub.payment.service.infrastructure.persistence.jpa.booking.entity.BookingJpaEntity;

@Component
public class BookingPersistenceMapper {

    public BookingAggregate toDomain(BookingJpaEntity bookingJpaEntity) {
        return BookingAggregate.builder()
                .id(bookingJpaEntity.getId())
                .bookingCode(bookingJpaEntity.getBookingCode())
                .routeId(bookingJpaEntity.getRouteId())
                .customerId(bookingJpaEntity.getCustomerId())
                .seatCount(bookingJpaEntity.getSeatCount())
                .totalAmount(bookingJpaEntity.getTotalAmount())
                .currency(bookingJpaEntity.getCurrency())
                .status(bookingJpaEntity.getStatus())
                .heldAt(bookingJpaEntity.getHeldAt())
                .holdUntil(bookingJpaEntity.getHoldUntil())
                .cancelledAt(bookingJpaEntity.getCancelledAt())
                .note(bookingJpaEntity.getNote())
                .creator(bookingJpaEntity.getCreator())
                .createdBy(bookingJpaEntity.getCreatedBy())
                .createdAt(bookingJpaEntity.getCreatedAt())
                .updatedBy(bookingJpaEntity.getUpdatedBy())
                .updatedAt(bookingJpaEntity.getUpdatedAt())
                .build();
    }
}
