package vn.com.routex.hub.payment.service.application.services.merchant.impl;

import org.springframework.stereotype.Service;
import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlCommand;
import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlResult;
import vn.com.routex.hub.payment.service.application.services.merchant.PaymentMerchantService;
import vn.com.routex.hub.payment.service.domain.payment.PaymentMethod;

@Service
public class VNPayMerchantServiceImpl implements PaymentMerchantService {
    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.VNPAY;
    }

    @Override
    public GetPaymentUrlResult getPaymentUrl(GetPaymentUrlCommand command) {
        return null;
    }
}
