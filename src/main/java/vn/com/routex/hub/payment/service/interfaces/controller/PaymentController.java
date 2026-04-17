package vn.com.routex.hub.payment.service.interfaces.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.routex.hub.payment.service.application.services.VNPayService;
import vn.com.routex.hub.payment.service.interfaces.model.result.ApiResult;
import vn.com.routex.hub.payment.service.interfaces.model.vnpay.PaymentRequest;
import vn.com.routex.hub.payment.service.interfaces.model.vnpay.PaymentResponse;

import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.API_PATH;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.API_VERSION;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.PAYMENT_PATH;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.SUCCESS_CODE;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.SUCCESS_MESSAGE;

@RestController
@RequestMapping(API_PATH + API_VERSION + PAYMENT_PATH)
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;

    @PostMapping("/create-payment-url")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody @Valid PaymentRequest paymentRequest, HttpServletRequest request) {
        try {
            String paymentUrl = vnPayService.createPaymentUrl(paymentRequest, request);

            PaymentResponse response = PaymentResponse.builder()
                    .requestId(paymentRequest.getRequestId())
                    .requestDateTime(paymentRequest.getRequestDateTime())
                    .channel(paymentRequest.getChannel())
                    .result(ApiResult.builder()
                            .responseCode(SUCCESS_CODE)
                            .description(SUCCESS_MESSAGE).build())
                    .data(PaymentResponse.PaymentResponseData.builder()
                            .paymentUrl(paymentUrl)
                            .build())
                    .build();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

}
