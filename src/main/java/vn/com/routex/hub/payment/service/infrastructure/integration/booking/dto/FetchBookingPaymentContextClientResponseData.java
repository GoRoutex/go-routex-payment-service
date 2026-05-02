package vn.com.routex.hub.payment.service.infrastructure.integration.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.routex.hub.payment.service.domain.booking.BookingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FetchBookingPaymentContextClientResponseData {

    private String bookingId;
    private String bookingCode;
    private BigDecimal totalAmount;
    private String currency;
    private BookingStatus bookingStatus;
    private OffsetDateTime holdUntil;
}
