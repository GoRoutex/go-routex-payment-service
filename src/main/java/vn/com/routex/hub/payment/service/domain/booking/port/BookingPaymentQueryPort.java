package vn.com.routex.hub.payment.service.domain.booking.port;

import vn.com.routex.hub.payment.service.application.command.common.RequestContext;
import vn.com.routex.hub.payment.service.domain.booking.model.BookingPaymentContext;

public interface BookingPaymentQueryPort {

    BookingPaymentContext getBookingPaymentContext(String bookingCode, RequestContext context);
}
