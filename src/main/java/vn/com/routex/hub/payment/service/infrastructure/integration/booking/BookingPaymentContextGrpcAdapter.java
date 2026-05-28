package vn.com.routex.hub.payment.service.infrastructure.integration.booking;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import vn.com.routex.hub.payment.service.application.command.common.RequestContext;
import vn.com.routex.hub.payment.service.domain.booking.BookingStatus;
import vn.com.routex.hub.payment.service.domain.booking.model.BookingPaymentContext;
import vn.com.routex.hub.payment.service.domain.booking.port.BookingPaymentQueryPort;
import vn.com.routex.hub.payment.service.infrastructure.persistence.exception.BusinessException;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.ExceptionUtils;
import vn.com.routex.hub.grpc.BookingGrpcServiceGrpc;
import vn.com.routex.hub.grpc.BookingRequestContext;
import vn.com.routex.hub.grpc.FetchBookingPaymentContextRequest;
import vn.com.routex.hub.grpc.FetchBookingPaymentContextResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.BOOKING_CODE_NOT_FOUND;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.TIMEOUT_ERROR;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.SYSTEM_ERROR;

@Component
@RequiredArgsConstructor
public class BookingPaymentContextGrpcAdapter implements BookingPaymentQueryPort {

    @GrpcClient("bookingService")
    private BookingGrpcServiceGrpc.BookingGrpcServiceBlockingStub bookingServiceStub;

    @Override
    public BookingPaymentContext getBookingPaymentContext(String bookingCode, RequestContext context) {
        try {
            FetchBookingPaymentContextResponse response = bookingServiceStub.fetchBookingPaymentContext(
                    FetchBookingPaymentContextRequest.newBuilder()
                            .setBookingCode(bookingCode)
                            .setContext(BookingRequestContext.newBuilder()
                                    .setRequestId(context.requestId() != null ? context.requestId() : "")
                                    .setRequestDateTime(context.requestDateTime() != null ? context.requestDateTime() : "")
                                    .setChannel(context.channel() != null ? context.channel() : "")
                                    .build())
                            .build()
            );

            if (response == null || response.getBookingId().isEmpty()) {
                throw bookingNotFound(bookingCode, context);
            }

            return BookingPaymentContext.builder()
                    .bookingId(response.getBookingId())
                    .bookingCode(response.getBookingCode())
                    .totalAmount(response.getTotalAmount().isEmpty() ? BigDecimal.ZERO : new BigDecimal(response.getTotalAmount()))
                    .currency(response.getCurrency())
                    .bookingStatus(response.getBookingStatus().isEmpty() ? null : BookingStatus.valueOf(response.getBookingStatus()))
                    .holdUntil(response.getHoldUntil().isEmpty() ? null : OffsetDateTime.parse(response.getHoldUntil()))
                    .build();
        } catch (StatusRuntimeException ex) {
            if (ex.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw bookingNotFound(bookingCode, context);
            }
            throw translateGrpcException(ex, context);
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

    private BusinessException translateGrpcException(StatusRuntimeException ex, RequestContext context) {
        String errorMsg = ex.getStatus().getDescription();
        if (errorMsg == null) {
            errorMsg = ex.getMessage();
        }

        String responseCode = SYSTEM_ERROR;
        if (ex.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
            responseCode = TIMEOUT_ERROR;
        }

        return new BusinessException(
                context.requestId(),
                context.requestDateTime(),
                context.channel(),
                ExceptionUtils.buildResultResponse(responseCode, errorMsg)
        );
    }
}
