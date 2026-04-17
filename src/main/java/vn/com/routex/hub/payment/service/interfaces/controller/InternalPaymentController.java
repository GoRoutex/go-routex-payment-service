package vn.com.routex.hub.payment.service.interfaces.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.routex.hub.payment.service.application.command.payment.CheckoutCommand;
import vn.com.routex.hub.payment.service.application.command.payment.CheckoutResult;
import vn.com.routex.hub.payment.service.application.command.payment.CreatePaymentSessionCommand;
import vn.com.routex.hub.payment.service.application.command.payment.CreatePaymentSessionResult;
import vn.com.routex.hub.payment.service.application.command.payment.RequestMetadata;
import vn.com.routex.hub.payment.service.application.services.PaymentApplicationService;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.DateTimeUtils;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.HttpResponseUtil;
import vn.com.routex.hub.payment.service.interfaces.model.payment.CheckoutResponse;
import vn.com.routex.hub.payment.service.interfaces.model.payment.CreatePaymentSessionRequest;
import vn.com.routex.hub.payment.service.interfaces.model.payment.CreatePaymentSessionResponse;

import java.util.UUID;

import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.API_PATH;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.API_VERSION;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.CHECKOUT_PATH;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.PAYMENT_PATH;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.PAY_SESSION_PATH;

@RequestMapping(API_PATH + API_VERSION + PAYMENT_PATH)
@RestController
@RequiredArgsConstructor
public class InternalPaymentController {

    private final PaymentApplicationService paymentApplicationService;

    @PostMapping(PAY_SESSION_PATH)
    public ResponseEntity<CreatePaymentSessionResponse> createPaymentSession(@Valid @RequestBody CreatePaymentSessionRequest createPaymentSessionRequest) {
        CreatePaymentSessionResult result = paymentApplicationService.createPaymentSession(
                CreatePaymentSessionCommand.builder()
                        .metadata(toMetadata(createPaymentSessionRequest))
                        .bookingId(createPaymentSessionRequest.getData().getBookingId())
                        .customerId(createPaymentSessionRequest.getData().getCustomerId())
                        .build()
        );
        return HttpResponseUtil.buildResponse(createPaymentSessionRequest, toCreatePaymentSessionResponse(result));
    }

    @GetMapping(CHECKOUT_PATH)
    public ResponseEntity<CheckoutResponse> checkout(
            @RequestParam String paymentId,
            @RequestParam String token) {
        CreatePaymentSessionRequest request = CreatePaymentSessionRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .requestDateTime(DateTimeUtils.now())
                .channel("ONL")
                .build();
        CheckoutResult result = paymentApplicationService.checkout(
                CheckoutCommand.builder()
                        .metadata(toMetadata(request))
                        .paymentId(paymentId)
                        .token(token)
                        .build()
        );
        return HttpResponseUtil.buildResponse(request, toCheckoutResponse(result));
    }

    private RequestMetadata toMetadata(CreatePaymentSessionRequest request) {
        return RequestMetadata.builder()
                .requestId(request.getRequestId())
                .requestDateTime(request.getRequestDateTime())
                .channel(request.getChannel())
                .build();
    }

    private CreatePaymentSessionResponse toCreatePaymentSessionResponse(CreatePaymentSessionResult result) {
        return CreatePaymentSessionResponse.builder()
                .result(result.result())
                .data(CreatePaymentSessionResponse.CreatePaymentSessionResponseData.builder()
                        .paymentId(result.paymentId())
                        .bookingId(result.bookingId())
                        .amount(result.amount())
                        .currency(result.currency())
                        .status(result.status())
                        .qrContent(result.qrContent())
                        .checkoutUrl(result.checkoutUrl())
                        .expiresAt(result.expiresAt())
                        .build())
                .build();
    }

    private CheckoutResponse toCheckoutResponse(CheckoutResult result) {
        return CheckoutResponse.builder()
                .result(result.result())
                .data(CheckoutResponse.CheckoutResponseData.builder()
                        .paymentId(result.paymentId())
                        .status(result.status())
                        .amount(result.amount())
                        .currency(result.currency())
                        .paidAt(result.paidAt())
                        .build())
                .build();
    }
}
