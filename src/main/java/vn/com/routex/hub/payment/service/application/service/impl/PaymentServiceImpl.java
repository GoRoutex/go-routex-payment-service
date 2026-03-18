package vn.com.routex.hub.payment.service.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.com.routex.hub.payment.service.application.service.PaymentService;
import vn.com.routex.hub.payment.service.application.service.QrService;
import vn.com.routex.hub.payment.service.controller.model.payment.CheckoutRequest;
import vn.com.routex.hub.payment.service.controller.model.payment.CheckoutResponse;
import vn.com.routex.hub.payment.service.controller.model.payment.CreatePaymentSessionRequest;
import vn.com.routex.hub.payment.service.controller.model.payment.CreatePaymentSessionResponse;
import vn.com.routex.hub.payment.service.controller.model.result.ApiResult;
import vn.com.routex.hub.payment.service.domain.booking.Booking;
import vn.com.routex.hub.payment.service.domain.booking.BookingRepository;
import vn.com.routex.hub.payment.service.domain.booking.BookingStatus;
import vn.com.routex.hub.payment.service.domain.booking.PaymentStatus;
import vn.com.routex.hub.payment.service.domain.payment.Payment;
import vn.com.routex.hub.payment.service.domain.payment.PaymentRepository;
import vn.com.routex.hub.payment.service.infrastructure.kafka.config.KafkaEventPublisher;
import vn.com.routex.hub.payment.service.infrastructure.kafka.event.PaymentFailedEvent;
import vn.com.routex.hub.payment.service.infrastructure.kafka.event.PaymentSuccessEvent;
import vn.com.routex.hub.payment.service.infrastructure.persistence.exception.BusinessException;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.ExceptionUtils;

import java.time.OffsetDateTime;
import java.util.UUID;

import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.BOOKING_RECORD_NOT_FOUND;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.INVALID_DATA_ERROR;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.PROCESS_FAIL_ERROR;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.SUCCESS_CODE;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.SUCCESS_MESSAGE;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final QrService qrService;
    private final KafkaEventPublisher kafkaEventPublisher;

    @Value("${spring.kafka.topics.payment-success}")
    private String paymentSuccessTopic;

    @Value("${spring.kafka.topics.payment-failed}")
    private String paymentFailedTopic;

    @Value("${spring.kafka.events.payment-completed}")
    private String paymentCompletedEvent;

    @Value("${spring.kafka.events.payment-failed}")
    private String paymentFailedEvent;

    private static final String PAYMENT_URL = "http://localhost:8080/api/v1/payment-service/checkout";

    @Override
    public CreatePaymentSessionResponse createPaymentSession(CreatePaymentSessionRequest request) {
        Booking booking = bookingRepository.findById(request.getData().getBookingId())
                .orElseThrow(() -> new BusinessException(request.getRequestId(), request.getRequestDateTime(), request.getChannel(),
                        ExceptionUtils.buildResultResponse(RECORD_NOT_FOUND, BOOKING_RECORD_NOT_FOUND)));

        if(!BookingStatus.PENDING_PAYMENT.equals(booking.getStatus())) {
            throw new BusinessException(request.getRequestId(), request.getRequestDateTime(), request.getChannel(),
                    ExceptionUtils.buildResultResponse(INVALID_DATA_ERROR, "Booking status is not HELD"));
        }

        if(booking.getHoldUntil().isBefore(OffsetDateTime.now())) {
            throw new BusinessException(request.getRequestId(), request.getRequestDateTime(), request.getChannel(),
                    ExceptionUtils.buildResultResponse(INVALID_DATA_ERROR, "Booking Session is expired"));
        }

        Payment existingPayment = paymentRepository.findByBookingIdAndStatus(request.getData().getBookingId(), PaymentStatus.PENDING)
                .orElse(null);

        if(existingPayment != null && !existingPayment.getExpiredAt().isBefore(OffsetDateTime.now())) {
            String qrBase64 = qrService.generateBase64Png(existingPayment.getCheckoutUrl(), 300, 300);
            return getCreatePaymentSessionResponse(qrBase64, request, existingPayment);
        }

        String paymentId = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString();
        String paymentURL = PAYMENT_URL + "?paymentId=" + paymentId + "&token=" + token;
        String qrBase64 = qrService.generateBase64Png(paymentURL, 300, 300);
        Payment payment = Payment
                .builder()
                .id(paymentId)
                .code("")
                .bookingId(request.getData().getBookingId())
                .amount(booking.getTotalAmount())
                .currency(booking.getCurrency())
                .status(PaymentStatus.PENDING)
                .paymentToken(token)
                .checkoutUrl(paymentURL)
                .expiredAt(booking.getHoldUntil())
                .createdAt(OffsetDateTime.now())
                .build();

        paymentRepository.save(payment);
        bookingRepository.save(booking);

        return getCreatePaymentSessionResponse(qrBase64, request, payment);
    }

    @Override
    public CheckoutResponse checkout(CheckoutRequest request) {

        Payment payment = paymentRepository.findById(request.getData().getPaymentId())
                .orElseThrow(() -> new BusinessException(request.getRequestId(), request.getRequestDateTime(), request.getChannel(),
                        ExceptionUtils.buildResultResponse(INVALID_DATA_ERROR, "Payment not found")));

        try {
            if (!request.getData().getToken().equalsIgnoreCase(payment.getPaymentToken())) {
                throw new BusinessException(request.getRequestId(), request.getRequestDateTime(), request.getChannel(),
                        ExceptionUtils.buildResultResponse(INVALID_DATA_ERROR, "Payment Token is not correct"));
            }

            payment.setPaidAt(OffsetDateTime.now());
            payment.setStatus(PaymentStatus.PAID);
            payment.setUpdatedAt(OffsetDateTime.now());
            paymentRepository.save(payment);

            PaymentSuccessEvent event = PaymentSuccessEvent.builder()
                    .paymentId(request.getData().getPaymentId())
                    .amount(payment.getAmount())
                    .bookingId(payment.getBookingId())
                    .currency(payment.getCurrency())
                    .status(payment.getStatus())
                    .paidAt(payment.getPaidAt())
                    .build();

            kafkaEventPublisher.publish(request, paymentSuccessTopic, paymentCompletedEvent, request.getData().getPaymentId(), event);

            return CheckoutResponse.builder()
                    .requestId(request.getRequestId())
                    .requestDateTime(request.getRequestDateTime())
                    .channel(request.getChannel())
                    .result(ApiResult.builder()
                            .responseCode(SUCCESS_CODE)
                            .description(SUCCESS_MESSAGE)
                            .build())
                    .data(CheckoutResponse.CheckoutResponseData
                            .builder()
                            .paymentId(request.getData().getPaymentId())
                            .status(PaymentStatus.PAID)
                            .amount(payment.getAmount())
                            .currency(payment.getCurrency())
                            .paidAt(payment.getPaidAt())
                            .build())
                    .build();
        } catch(Exception e) {

            if(payment != null) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailedAt(OffsetDateTime.now());
                payment.setUpdatedAt(OffsetDateTime.now());
                paymentRepository.save(payment);
            }

            PaymentFailedEvent event = PaymentFailedEvent.builder()
                    .paymentId(request.getData().getPaymentId())
                    .bookingId(request.getData().getBookingId())
                    .status(PaymentStatus.FAILED)
                    .reason(e.getMessage())
                    .build();

            kafkaEventPublisher.publish(request, paymentFailedTopic, paymentFailedEvent, request.getData().getPaymentId(), event);

            throw new BusinessException(request.getRequestId(), request.getRequestDateTime(), request.getChannel(),
                    ExceptionUtils.buildResultResponse(PROCESS_FAIL_ERROR, "Payment failed"));
        }
    }

    private CreatePaymentSessionResponse getCreatePaymentSessionResponse(String qrBase64, CreatePaymentSessionRequest request, Payment existingPayment) {
        return CreatePaymentSessionResponse.builder()
                .requestId(request.getRequestId())
                .requestDateTime(request.getRequestDateTime())
                .channel(request.getChannel())
                .result(ApiResult.builder()
                        .responseCode(SUCCESS_CODE)
                        .description(SUCCESS_MESSAGE)
                        .build())
                .data(CreatePaymentSessionResponse.CreatePaymentSessionResponseData
                        .builder()
                        .bookingId(existingPayment.getBookingId())
                        .paymentId(existingPayment.getId())
                        .amount(existingPayment.getAmount())
                        .checkoutUrl(existingPayment.getCheckoutUrl())
                        .qrContent(qrBase64)
                        .currency(existingPayment.getCurrency())
                        .expiresAt(existingPayment.getExpiredAt())
                        .status(existingPayment.getStatus())
                        .build())
                .build();
    }
}
