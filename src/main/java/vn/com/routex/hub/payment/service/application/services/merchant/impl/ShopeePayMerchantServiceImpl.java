package vn.com.routex.hub.payment.service.application.services.merchant.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlCommand;
import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlResult;
import vn.com.routex.hub.payment.service.application.services.merchant.PaymentMerchantService;
import vn.com.routex.hub.payment.service.domain.booking.BookingStatus;
import vn.com.routex.hub.payment.service.domain.booking.PaymentStatus;
import vn.com.routex.hub.payment.service.domain.booking.model.Booking;
import vn.com.routex.hub.payment.service.domain.booking.port.BookingRepositoryPort;
import vn.com.routex.hub.payment.service.domain.merchant.MerchantSessionStatus;
import vn.com.routex.hub.payment.service.domain.merchant.model.MerchantSessionAggregate;
import vn.com.routex.hub.payment.service.domain.merchant.port.MerchantSessionRepositoryPort;
import vn.com.routex.hub.payment.service.domain.payment.PaymentMethod;
import vn.com.routex.hub.payment.service.domain.payment.model.PaymentAggregate;
import vn.com.routex.hub.payment.service.domain.payment.port.PaymentRepositoryPort;
import vn.com.routex.hub.payment.service.domain.payment.port.QrCodeGeneratorPort;
import vn.com.routex.hub.payment.service.infrastructure.persistence.exception.BusinessException;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.ExceptionUtils;

import java.time.OffsetDateTime;
import java.util.UUID;

import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.BOOKING_CODE_NOT_FOUND;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.INVALID_DATA_ERROR;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.RECORD_NOT_FOUND;


@Service
@RequiredArgsConstructor
public class ShopeePayMerchantServiceImpl implements PaymentMerchantService {

    private final BookingRepositoryPort bookingRepositoryPort;
    private final QrCodeGeneratorPort qrCodeGeneratorPort;
    private final PaymentRepositoryPort paymentRepositoryPort;
    private final MerchantSessionRepositoryPort merchantSessionRepositoryPort;

    @Value("${app.payment.checkout-url}")
    private String checkoutBaseUrl;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.SHOPEEPAY;
    }

    @Override
    public GetPaymentUrlResult getPaymentUrl(GetPaymentUrlCommand command) {

        Booking bookingAggregate = bookingRepositoryPort.findByBookingCode(command.bookingCode())
                .orElseThrow(() -> new BusinessException(command.context().requestId(), command.context().requestDateTime(), command.context().channel(),
                        ExceptionUtils.buildResultResponse(RECORD_NOT_FOUND, String.format(BOOKING_CODE_NOT_FOUND, command.bookingCode()))));
        if(!BookingStatus.PENDING_PAYMENT.equals(bookingAggregate.getStatus())) {
            throw new BusinessException(command.context().requestId(), command.context().requestDateTime(), command.context().channel(),
                    ExceptionUtils.buildResultResponse(INVALID_DATA_ERROR, "Booking Status is not Pending Payment"));
        }
        if(!bookingAggregate.getHoldUntil().isAfter(OffsetDateTime.now())) {
            throw new BusinessException(command.context().requestId(), command.context().requestDateTime(), command.context().channel(),
                    ExceptionUtils.buildResultResponse(INVALID_DATA_ERROR, "Booking Session is expired"));
        }
        OffsetDateTime now = OffsetDateTime.now();
        PaymentAggregate payment = getOrCreatePendingPayment(command, bookingAggregate, now);
        MerchantSessionAggregate session = getOrCreateReusableMerchantSession(command, payment, bookingAggregate, now);
        String qrCodeUrl = qrCodeGeneratorPort.generateBase64Png(
                session.getCheckoutUrl(),
                300,
                300
        );
        return GetPaymentUrlResult.builder()
                .bookingCode(command.bookingCode())
                .amount(payment.getAmount())
                .method(command.method())
                .qrCodeUrl(qrCodeUrl)
                .paymentUrl(session.getCheckoutUrl())
                .deeplink(session.getDeeplink())
                .expiredTime(session.getExpiredAt())
                .build();
    }

    private MerchantSessionAggregate getOrCreateReusableMerchantSession(
            GetPaymentUrlCommand command,
            PaymentAggregate aggregate,
            Booking booking,
            OffsetDateTime now
    ) {
        return merchantSessionRepositoryPort.findLatestByPaymentIdAndStatus(aggregate.getId(), MerchantSessionStatus.CREATED)
                .filter(session -> session.isReusable(now))
                .orElseGet(() -> {
                    int nextAttemptNo = merchantSessionRepositoryPort.countByPaymentId(aggregate.getId()) + 1;
                    String sessionId = UUID.randomUUID().toString();
                    String token = UUID.randomUUID().toString();
                    String checkoutUrl = checkoutBaseUrl
                            + "?paymentId=" + aggregate.getId()
                            + "&sessionId=" + sessionId
                            + "&token=" + token;
                    MerchantSessionAggregate session = MerchantSessionAggregate.builder()
                            .id(sessionId)
                            .paymentId(aggregate.getId())
                            .paymentMerchant(command.method())
                            .status(MerchantSessionStatus.CREATED)
                            .attemptNo(nextAttemptNo)
                            .merchantTxnRef(sessionId)
                            .checkoutUrl(checkoutUrl)
                            .deeplink("")
                            .expiredAt(booking.getHoldUntil())
                            .createdAt(now)
                            .build();
                    merchantSessionRepositoryPort.save(session);
                    return session;
                });
    }
    private PaymentAggregate getOrCreatePendingPayment(
            GetPaymentUrlCommand command,
            Booking booking,
            OffsetDateTime now
    ) {
        return paymentRepositoryPort
                .findByBookingCodeAndMethodAndStatus(
                        command.bookingCode(),
                        command.method(),
                        PaymentStatus.PENDING
                )
                .filter(payment -> payment.isReusablePendingPayment(now))
                .orElseGet(() -> {
                    PaymentAggregate payment = PaymentAggregate.builder()
                            .id(UUID.randomUUID().toString())
                            .bookingCode(command.bookingCode())
                            .method(command.method())
                            .amount(booking.getTotalAmount())
                            .currency(booking.getCurrency())
                            .status(PaymentStatus.PENDING)
                            .expiredAt(booking.getHoldUntil())
                            .createdAt(now)
                            .build();

                    return paymentRepositoryPort.save(payment);
                });
    }
}
