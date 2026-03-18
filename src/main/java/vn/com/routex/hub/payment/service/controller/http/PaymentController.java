package vn.com.routex.hub.payment.service.controller.http;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.routex.hub.payment.service.application.facade.PaymentFacade;
import vn.com.routex.hub.payment.service.controller.model.payment.CheckoutRequest;
import vn.com.routex.hub.payment.service.controller.model.payment.CheckoutResponse;
import vn.com.routex.hub.payment.service.controller.model.payment.CreatePaymentSessionRequest;
import vn.com.routex.hub.payment.service.controller.model.payment.CreatePaymentSessionResponse;

import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.API_PATH;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.API_VERSION;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.CHECKOUT_PATH;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.PAYMENT_PATH;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.PAY_SESSION_PATH;

@RequestMapping(API_PATH + API_VERSION + PAYMENT_PATH)
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @PostMapping(PAY_SESSION_PATH)
    public ResponseEntity<CreatePaymentSessionResponse> createPaymentSession(@Valid @RequestBody CreatePaymentSessionRequest createPaymentSessionRequest) {
        return paymentFacade.createPaymentSession(createPaymentSessionRequest);
    }

    @GetMapping(CHECKOUT_PATH)
    public ResponseEntity<CheckoutResponse> checkout(
            @RequestParam String paymentId,
            @RequestParam String token) {
        return paymentFacade.checkout(paymentId, token);
    }
}
