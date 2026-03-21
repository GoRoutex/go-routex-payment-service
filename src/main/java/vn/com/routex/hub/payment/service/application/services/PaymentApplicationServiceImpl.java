package vn.com.routex.hub.payment.service.application.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.com.routex.hub.payment.service.application.dto.payment.CheckoutCommand;
import vn.com.routex.hub.payment.service.application.dto.payment.CheckoutResult;
import vn.com.routex.hub.payment.service.application.dto.payment.CreatePaymentSessionCommand;
import vn.com.routex.hub.payment.service.application.dto.payment.CreatePaymentSessionResult;
import vn.com.routex.hub.payment.service.domain.booking.PaymentStatus;
import vn.com.routex.hub.payment.service.domain.booking.model.BookingAggregate;
import vn.com.routex.hub.payment.service.domain.booking.port.BookingRepositoryPort;
import vn.com.routex.hub.payment.service.domain.payment.model.PaymentAggregate;
import vn.com.routex.hub.payment.service.domain.payment.port.PaymentEventPublisherPort;
import vn.com.routex.hub.payment.service.domain.payment.port.PaymentRepositoryPort;
import vn.com.routex.hub.payment.service.domain.payment.port.QrCodeGeneratorPort;
import vn.com.routex.hub.payment.service.infrastructure.persistence.exception.BusinessException;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.ExceptionUtils;
import vn.com.routex.hub.payment.service.interfaces.models.result.ApiResult;

import java.time.OffsetDateTime;
import java.util.UUID;

import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.BOOKING_RECORD_NOT_FOUND;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.INVALID_DATA_ERROR;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.PROCESS_FAIL_ERROR;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.SUCCESS_CODE;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.SUCCESS_MESSAGE;

@Service
@RequiredArgsConstructor
public class PaymentApplicationServiceImpl implements PaymentApplicationService {

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final BookingRepositoryPort bookingRepositoryPort;
    private final QrCodeGeneratorPort qrCodeGeneratorPort;
    private final PaymentEventPublisherPort paymentEventPublisherPort;

    @Value("${app.payment.checkout-base-url:http://localhost:8080/api/v1/payment-service/checkout}")
    private String checkoutBaseUrl;

    @Override
    public CreatePaymentSessionResult createPaymentSession(CreatePaymentSessionCommand command) {
        BookingAggregate bookingAggregate = bookingRepositoryPort.findById(command.bookingId())
                .orElseThrow(() -> businessException(command, RECORD_NOT_FOUND, BOOKING_RECORD_NOT_FOUND));

        if (!bookingAggregate.isPendingPayment()) {
            throw businessException(command, INVALID_DATA_ERROR, "Booking status is not HELD");
        }
        if (bookingAggregate.isHoldExpired(OffsetDateTime.now())) {
            throw businessException(command, INVALID_DATA_ERROR, "Booking Session is expired");
        }

        PaymentAggregate existingPayment = paymentRepositoryPort
                .findByBookingIdAndStatus(command.bookingId(), PaymentStatus.PENDING)
                .orElse(null);

        if (existingPayment != null && existingPayment.isReusablePendingPayment(OffsetDateTime.now())) {
            return buildCreatePaymentSessionResult(
                    qrCodeGeneratorPort.generateBase64Png(existingPayment.getCheckoutUrl(), 300, 300),
                    existingPayment
            );
        }

        String paymentId = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString();
        String checkoutUrl = checkoutBaseUrl + "?paymentId=" + paymentId + "&token=" + token;
        PaymentAggregate paymentAggregate = PaymentAggregate.builder()
                .id(paymentId)
                .code("")
                .bookingId(command.bookingId())
                .amount(bookingAggregate.getTotalAmount())
                .currency(bookingAggregate.getCurrency())
                .status(PaymentStatus.PENDING)
                .paymentToken(token)
                .checkoutUrl(checkoutUrl)
                .expiredAt(bookingAggregate.getHoldUntil())
                .createdAt(OffsetDateTime.now())
                .build();

        PaymentAggregate savedPayment = paymentRepositoryPort.save(paymentAggregate);
        return buildCreatePaymentSessionResult(
                qrCodeGeneratorPort.generateBase64Png(savedPayment.getCheckoutUrl(), 300, 300),
                savedPayment
        );
    }

    @Override
    public CheckoutResult checkout(CheckoutCommand command) {
        PaymentAggregate paymentAggregate = paymentRepositoryPort.findById(command.paymentId())
                .orElseThrow(() -> businessException(command, INVALID_DATA_ERROR, "Payment not found"));

        try {
            if (!command.token().equalsIgnoreCase(paymentAggregate.getPaymentToken())) {
                throw businessException(command, INVALID_DATA_ERROR, "Payment Token is not correct");
            }

            paymentAggregate.markPaid(OffsetDateTime.now());
            PaymentAggregate savedPayment = paymentRepositoryPort.save(paymentAggregate);
            paymentEventPublisherPort.publishPaymentSucceeded(command.metadata(), savedPayment);

            return CheckoutResult.builder()
                    .result(successResult())
                    .paymentId(savedPayment.getId())
                    .status(savedPayment.getStatus())
                    .amount(savedPayment.getAmount())
                    .currency(savedPayment.getCurrency())
                    .paidAt(savedPayment.getPaidAt())
                    .build();
        } catch (Exception ex) {
            paymentAggregate.markFailed(OffsetDateTime.now(), ex.getMessage());
            PaymentAggregate failedPayment = paymentRepositoryPort.save(paymentAggregate);
            paymentEventPublisherPort.publishPaymentFailed(command.metadata(), failedPayment, ex.getMessage());
            throw businessException(command, PROCESS_FAIL_ERROR, "Payment failed");
        }
    }

    private CreatePaymentSessionResult buildCreatePaymentSessionResult(String qrContent, PaymentAggregate paymentAggregate) {
        return CreatePaymentSessionResult.builder()
                .result(successResult())
                .paymentId(paymentAggregate.getId())
                .bookingId(paymentAggregate.getBookingId())
                .amount(paymentAggregate.getAmount())
                .currency(paymentAggregate.getCurrency())
                .status(paymentAggregate.getStatus())
                .qrContent(qrContent)
                .checkoutUrl(paymentAggregate.getCheckoutUrl())
                .expiresAt(paymentAggregate.getExpiredAt())
                .build();
    }

    private ApiResult successResult() {
        return ApiResult.builder()
                .responseCode(SUCCESS_CODE)
                .description(SUCCESS_MESSAGE)
                .build();
    }

    private BusinessException businessException(CreatePaymentSessionCommand command, String responseCode, String description) {
        return new BusinessException(
                command.metadata().requestId(),
                command.metadata().requestDateTime(),
                command.metadata().channel(),
                ExceptionUtils.buildResultResponse(responseCode, description)
        );
    }

    private BusinessException businessException(CheckoutCommand command, String responseCode, String description) {
        return new BusinessException(
                command.metadata().requestId(),
                command.metadata().requestDateTime(),
                command.metadata().channel(),
                ExceptionUtils.buildResultResponse(responseCode, description)
        );
    }
}
