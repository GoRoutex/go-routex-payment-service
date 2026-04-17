package vn.com.routex.hub.payment.service.application.services;

import vn.com.routex.hub.payment.service.application.command.payment.CheckoutCommand;
import vn.com.routex.hub.payment.service.application.command.payment.CheckoutResult;
import vn.com.routex.hub.payment.service.application.command.payment.CreatePaymentSessionCommand;
import vn.com.routex.hub.payment.service.application.command.payment.CreatePaymentSessionResult;

public interface PaymentApplicationService {

    CreatePaymentSessionResult createPaymentSession(CreatePaymentSessionCommand command);

    CheckoutResult checkout(CheckoutCommand command);
}
