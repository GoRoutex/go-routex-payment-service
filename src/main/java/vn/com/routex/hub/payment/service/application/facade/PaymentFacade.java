package vn.com.routex.hub.payment.service.application.facade;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import vn.com.routex.hub.payment.service.controller.model.payment.CheckoutRequest;
import vn.com.routex.hub.payment.service.controller.model.payment.CheckoutResponse;
import vn.com.routex.hub.payment.service.controller.model.payment.CreatePaymentSessionRequest;
import vn.com.routex.hub.payment.service.controller.model.payment.CreatePaymentSessionResponse;

public interface PaymentFacade {
    ResponseEntity<CreatePaymentSessionResponse> createPaymentSession(CreatePaymentSessionRequest request);

    ResponseEntity<CheckoutResponse> checkout(String paymentId, String token);
}
