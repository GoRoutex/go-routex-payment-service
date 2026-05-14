package vn.com.routex.hub.payment.service.application.services.merchant;

import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlCommand;
import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlResult;
import vn.com.routex.hub.payment.service.application.command.payment.PollingPaymentStatusCommand;
import vn.com.routex.hub.payment.service.application.command.payment.PollingPaymentStatusResult;

public interface PaymentOrchestrationService {

    GetPaymentUrlResult getPaymentUrl(GetPaymentUrlCommand command);

    PollingPaymentStatusResult pollingStatus(PollingPaymentStatusCommand command);
}
