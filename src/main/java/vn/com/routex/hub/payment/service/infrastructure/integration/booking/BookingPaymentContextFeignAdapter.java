package vn.com.routex.hub.payment.service.infrastructure.integration.booking;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.com.routex.hub.payment.service.application.command.common.RequestContext;
import vn.com.routex.hub.payment.service.domain.booking.model.BookingPaymentContext;
import vn.com.routex.hub.payment.service.domain.booking.port.BookingPaymentQueryPort;
import vn.com.routex.hub.payment.service.infrastructure.integration.booking.client.BookingServiceContextFeignClient;
import vn.com.routex.hub.payment.service.infrastructure.integration.booking.dto.FetchBookingPaymentContextClientRequest;
import vn.com.routex.hub.payment.service.infrastructure.integration.booking.dto.FetchBookingPaymentContextClientRequestData;
import vn.com.routex.hub.payment.service.infrastructure.integration.booking.dto.FetchBookingPaymentContextClientResponse;
import vn.com.routex.hub.payment.service.infrastructure.integration.booking.dto.FetchBookingPaymentContextClientResponseData;
import vn.com.routex.hub.payment.service.infrastructure.persistence.exception.BusinessException;
import vn.com.routex.hub.payment.service.infrastructure.persistence.exception.CustomFeignException;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.ExceptionUtils;

import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.BOOKING_CODE_NOT_FOUND;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.RECORD_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class BookingPaymentContextFeignAdapter implements BookingPaymentQueryPort {

    private final BookingServiceContextFeignClient bookingServiceContextFeignClient;

    @Override
    public BookingPaymentContext getBookingPaymentContext(String bookingCode, RequestContext context) {
        try {
            FetchBookingPaymentContextClientResponse response = bookingServiceContextFeignClient.fetchBookingPaymentContext(
                    FetchBookingPaymentContextClientRequest.builder()
                            .requestId(context.requestId())
                            .requestDateTime(context.requestDateTime())
                            .channel(context.channel())
                            .data(FetchBookingPaymentContextClientRequestData.builder()
                                    .bookingCode(bookingCode)
                                    .build())
                            .build()
            );

            if (response == null || response.getData() == null) {
                throw bookingNotFound(bookingCode, context);
            }

            FetchBookingPaymentContextClientResponseData data = response.getData();
            return BookingPaymentContext.builder()
                    .bookingId(data.getBookingId())
                    .bookingCode(data.getBookingCode())
                    .totalAmount(data.getTotalAmount())
                    .currency(data.getCurrency())
                    .bookingStatus(data.getBookingStatus())
                    .holdUntil(data.getHoldUntil())
                    .build();
        } catch (FeignException.NotFound ex) {
            throw bookingNotFound(bookingCode, context);
        } catch (BusinessException ex) {
            throw ex;
        } catch (CustomFeignException ex) {
            throw ex;
        }
    }

    private BusinessException bookingNotFound(String bookingCode, RequestContext context) {
        return new BusinessException(
                context.requestId(),
                context.requestDateTime(),
                context.channel(),
                ExceptionUtils.buildResultResponse(RECORD_NOT_FOUND, String.format(BOOKING_CODE_NOT_FOUND, bookingCode))
        );
    }
}
