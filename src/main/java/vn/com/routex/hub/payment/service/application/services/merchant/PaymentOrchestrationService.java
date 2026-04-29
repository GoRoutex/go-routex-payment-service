package vn.com.routex.hub.payment.service.application.services.merchant;

import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlCommand;
import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlResult;

public interface PaymentOrchestrationService {

    GetPaymentUrlResult getPaymentUrl(GetPaymentUrlCommand command);
}
