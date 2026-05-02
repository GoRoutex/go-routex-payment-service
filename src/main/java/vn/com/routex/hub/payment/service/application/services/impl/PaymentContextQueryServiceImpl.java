package vn.com.routex.hub.payment.service.application.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.com.routex.hub.payment.service.application.command.common.RequestContext;
import vn.com.routex.hub.payment.service.application.services.PaymentContextQueryService;
import vn.com.routex.hub.payment.service.domain.payment.model.PaymentAggregate;
import vn.com.routex.hub.payment.service.domain.payment.port.PaymentRepositoryPort;
import vn.com.routex.hub.payment.service.infrastructure.persistence.exception.BusinessException;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.ExceptionUtils;

import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.PAYMENT_NOT_FOUND;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.RECORD_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PaymentContextQueryServiceImpl implements PaymentContextQueryService {

    private final PaymentRepositoryPort paymentRepositoryPort;

    @Override
    public PaymentAggregate getPaymentContext(String bookingCode, RequestContext context) {
        return paymentRepositoryPort.findByBookingCode(bookingCode)
                .orElseThrow(() -> new BusinessException(
                        context.requestId(),
                        context.requestDateTime(),
                        context.channel(),
                        ExceptionUtils.buildResultResponse(RECORD_NOT_FOUND, String.format(PAYMENT_NOT_FOUND, bookingCode))
                ));
    }
}
