package vn.com.routex.hub.payment.service.application.services;

import vn.com.routex.hub.payment.service.application.dto.payment.CheckoutCommand;
import vn.com.routex.hub.payment.service.application.dto.payment.CheckoutResult;
import vn.com.routex.hub.payment.service.application.dto.payment.CreatePaymentSessionCommand;
import vn.com.routex.hub.payment.service.application.dto.payment.CreatePaymentSessionResult;

public interface PaymentApplicationService {

    CreatePaymentSessionResult createPaymentSession(CreatePaymentSessionCommand command);

    CheckoutResult checkout(CheckoutCommand command);
}
