package vn.com.routex.hub.payment.service.application.services.merchant;

import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlCommand;
import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlResult;
import vn.com.routex.hub.payment.service.domain.payment.PaymentMethod;

public interface PaymentMerchantService {

    PaymentMethod getPaymentMethod();

    GetPaymentUrlResult getPaymentUrl(GetPaymentUrlCommand command);
}
