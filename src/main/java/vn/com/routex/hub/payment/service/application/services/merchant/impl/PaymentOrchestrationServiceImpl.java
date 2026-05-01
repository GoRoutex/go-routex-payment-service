package vn.com.routex.hub.payment.service.application.services.merchant.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlCommand;
import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlResult;
import vn.com.routex.hub.payment.service.application.command.payment.PollingPaymentStatusCommand;
import vn.com.routex.hub.payment.service.application.command.payment.PollingPaymentStatusResult;
import vn.com.routex.hub.payment.service.application.services.merchant.PaymentMerchantService;
import vn.com.routex.hub.payment.service.application.services.merchant.PaymentOrchestrationService;
import vn.com.routex.hub.payment.service.application.services.merchant.factory.PaymentMerchantServiceFactory;
import vn.com.routex.hub.payment.service.domain.booking.PaymentStatus;
import vn.com.routex.hub.payment.service.domain.payment.model.PaymentAggregate;
import vn.com.routex.hub.payment.service.domain.payment.port.PaymentRepositoryPort;
import vn.com.routex.hub.payment.service.infrastructure.persistence.exception.BusinessException;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.ExceptionUtils;

import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.PAYMENT_NOT_FOUND;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.RECORD_NOT_FOUND;


@Service
@RequiredArgsConstructor
public class PaymentOrchestrationServiceImpl implements PaymentOrchestrationService {

    private final PaymentMerchantServiceFactory factory;
    private final PaymentRepositoryPort paymentRepositoryPort;

    @Override
    public GetPaymentUrlResult getPaymentUrl(GetPaymentUrlCommand command) {
        PaymentMerchantService service = factory.getService(command.method());
        return service.getPaymentUrl(command);
    }

    @Override
    public PollingPaymentStatusResult pollingStatus(PollingPaymentStatusCommand command) {

        PaymentAggregate paymentAggregate = paymentRepositoryPort.findByBookingCode(command.bookingCode())
                .orElseThrow(() -> new BusinessException(command.context().requestId(), command.context().requestDateTime(), command.context().channel(),
                        ExceptionUtils.buildResultResponse(RECORD_NOT_FOUND, String.format(PAYMENT_NOT_FOUND, command.bookingCode()))));

        if(PaymentStatus.FAILED.equals(paymentAggregate.getStatus())) {
            return PollingPaymentStatusResult.builder()
                    .status(PaymentStatus.FAILED)
                    .shouldStopPooling(true)
                    .build();
        }
        return PollingPaymentStatusResult.builder()
                .status(paymentAggregate.getStatus())
                .amount(paymentAggregate.getAmount())
                .bookingCode(command.bookingCode())
                .shouldStopPooling(paymentAggregate.getStatus().isFinal())
                .build();
    }
}
