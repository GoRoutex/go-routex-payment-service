package vn.com.routex.hub.payment.service.application.services.merchant.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlCommand;
import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlResult;
import vn.com.routex.hub.payment.service.application.services.merchant.PaymentMerchantService;
import vn.com.routex.hub.payment.service.application.services.merchant.PaymentOrchestrationService;
import vn.com.routex.hub.payment.service.application.services.merchant.factory.PaymentMerchantServiceFactory;


@Service
@RequiredArgsConstructor
public class PaymentOrchestrationServiceImpl implements PaymentOrchestrationService {

    private final PaymentMerchantServiceFactory factory;

    @Override
    public GetPaymentUrlResult getPaymentUrl(GetPaymentUrlCommand command) {
        PaymentMerchantService service = factory.getService(command.method());
        return service.getPaymentUrl(command);
    }
}
