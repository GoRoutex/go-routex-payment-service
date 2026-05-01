package vn.com.routex.hub.payment.service.application.services.merchant.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.com.go.routex.identity.security.log.SystemLog;
import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlCommand;
import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlResult;
import vn.com.routex.hub.payment.service.application.services.VNPayService;
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
public class VNPayMerchantServiceImpl implements PaymentMerchantService {

    private final BookingRepositoryPort bookingRepositoryPort;
    private final QrCodeGeneratorPort qrCodeGeneratorPort;
    private final PaymentRepositoryPort paymentRepositoryPort;
    private final MerchantSessionRepositoryPort merchantSessionRepositoryPort;
    private final VNPayService vnPayService;

    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.VNPAY;
    }

    @Override
    public GetPaymentUrlResult getPaymentUrl(GetPaymentUrlCommand command) {

        Booking bookingAggregate = bookingRepositoryPort.findByBookingCode(command.bookingCode())
                .orElseThrow(() -> new BusinessException(command.context().requestId(), command.context().requestDateTime(), command.context().channel(),
                        ExceptionUtils.buildResultResponse(RECORD_NOT_FOUND, String.format(BOOKING_CODE_NOT_FOUND, command.bookingCode()))));
        if (!BookingStatus.PENDING_PAYMENT.equals(bookingAggregate.getStatus())) {
            throw new BusinessException(command.context().requestId(), command.context().requestDateTime(), command.context().channel(),
                    ExceptionUtils.buildResultResponse(INVALID_DATA_ERROR, "Booking Status is not Pending Payment"));
        }
        if (!bookingAggregate.getHoldUntil().isAfter(OffsetDateTime.now())) {
            throw new BusinessException(command.context().requestId(), command.context().requestDateTime(), command.context().channel(),
                    ExceptionUtils.buildResultResponse(INVALID_DATA_ERROR, "Booking Session is expired"));
        }


        if (command.amount().compareTo(bookingAggregate.getTotalAmount()) != 0) {
            throw new BusinessException(command.context().requestId(), command.context().requestDateTime(), command.context().channel(),
                    ExceptionUtils.buildResultResponse(INVALID_DATA_ERROR, "Payment amount does not match booking total amount"));
        }
        OffsetDateTime now = OffsetDateTime.now();
        PaymentAggregate payment = getOrCreatePendingPayment(command, bookingAggregate, now);

        sLog.info("Payment aggregate: {}", payment);
        String checkoutUrl = buildCheckoutUrl(command, payment.getTxnRef());
        MerchantSessionAggregate session = getOrCreateReusableMerchantSession(command, payment, bookingAggregate, checkoutUrl, now);
        String qrCodeUrl = qrCodeGeneratorPort.generateBase64Png(
                checkoutUrl,
                300,
                300
        );
        return GetPaymentUrlResult.builder()
                .bookingCode(command.bookingCode())
                .amount(payment.getAmount())
                .method(command.method())
                .qrCodeUrl(qrCodeUrl)
                .paymentUrl(checkoutUrl)
                .deeplink(session.getDeeplink())
                .expiredTime(session.getExpiredAt())
                .build();
    }

    private String buildCheckoutUrl(GetPaymentUrlCommand command, String txnRef) {
        try {
            return vnPayService.createPaymentUrl(command, txnRef);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create VNPay checkout url", ex);
        }
    }

    private MerchantSessionAggregate getOrCreateReusableMerchantSession(
            GetPaymentUrlCommand command,
            PaymentAggregate aggregate,
            Booking booking,
            String checkoutUrl,
            OffsetDateTime now
    ) {
        return merchantSessionRepositoryPort.findLatestByPaymentIdAndStatus(aggregate.getId(), MerchantSessionStatus.CREATED)
                .filter(session -> session.isReusable(now))
                .orElseGet(() -> {
                    int nextAttemptNo = merchantSessionRepositoryPort.countByPaymentId(aggregate.getId()) + 1;
                    MerchantSessionAggregate session = MerchantSessionAggregate.builder()
                            .id(UUID.randomUUID().toString())
                            .paymentId(aggregate.getId())
                            .paymentMerchant(command.method())
                            .status(MerchantSessionStatus.CREATED)
                            .attemptNo(nextAttemptNo)
                            .merchantTxnRef(aggregate.getId())
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
                            .txnRef(UUID.randomUUID().toString())
                            .currency(booking.getCurrency())
                            .status(PaymentStatus.PENDING)
                            .createdAt(now)
                            .build();

                    return paymentRepositoryPort.save(payment);
                });
    }
}
