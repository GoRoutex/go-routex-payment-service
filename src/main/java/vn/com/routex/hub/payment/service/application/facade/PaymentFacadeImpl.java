package vn.com.routex.hub.payment.service.application.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.com.routex.hub.payment.service.application.service.PaymentService;
import vn.com.routex.hub.payment.service.controller.model.payment.CheckoutRequest;
import vn.com.routex.hub.payment.service.controller.model.payment.CheckoutResponse;
import vn.com.routex.hub.payment.service.controller.model.payment.CreatePaymentSessionRequest;
import vn.com.routex.hub.payment.service.controller.model.payment.CreatePaymentSessionResponse;
import vn.com.routex.hub.payment.service.infrastructure.persistence.exception.BusinessException;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.DateTimeUtils;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.ExceptionUtils;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.HttpResponseUtil;

import java.util.UUID;

import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.TIMEOUT_ERROR;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.TIMEOUT_ERROR_MESSAGE;

@RequiredArgsConstructor
@Service
public class PaymentFacadeImpl implements PaymentFacade {

    private final PaymentService paymentService;

    @Override
    public ResponseEntity<CreatePaymentSessionResponse> createPaymentSession(CreatePaymentSessionRequest request) {
        CreatePaymentSessionResponse response = paymentService.createPaymentSession(request);
        return HttpResponseUtil.buildResponse(request, response);
    }

    @Override
    public ResponseEntity<CheckoutResponse> checkout(String paymentId, String token) {

        CheckoutRequest request = CheckoutRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .requestDateTime(DateTimeUtils.now())
                .channel("ONL")
                .data(CheckoutRequest.CheckoutRequestData.builder()
                        .paymentId(paymentId)
                        .token(token)
                        .build())
                .build();

        CheckoutResponse response = paymentService.checkout(request);
        return HttpResponseUtil.buildResponse(request, response);
    }
}
