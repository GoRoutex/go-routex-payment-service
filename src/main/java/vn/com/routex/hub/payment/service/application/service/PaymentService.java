package vn.com.routex.hub.payment.service.application.service;

import vn.com.routex.hub.payment.service.controller.model.payment.CheckoutRequest;
import vn.com.routex.hub.payment.service.controller.model.payment.CheckoutResponse;
import vn.com.routex.hub.payment.service.controller.model.payment.CreatePaymentSessionRequest;
import vn.com.routex.hub.payment.service.controller.model.payment.CreatePaymentSessionResponse;

public interface PaymentService {

    CreatePaymentSessionResponse createPaymentSession(CreatePaymentSessionRequest request);

    CheckoutResponse checkout(CheckoutRequest request);
}
