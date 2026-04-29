package vn.com.routex.hub.payment.service.interfaces.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import vn.com.go.routex.identity.security.log.SystemLog;
import vn.com.routex.hub.payment.service.application.command.payment.CheckoutCommand;
import vn.com.routex.hub.payment.service.application.command.payment.CheckoutResult;
import vn.com.routex.hub.payment.service.application.command.payment.CreatePaymentSessionCommand;
import vn.com.routex.hub.payment.service.application.command.payment.CreatePaymentSessionResult;
import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlCommand;
import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlResult;
import vn.com.routex.hub.payment.service.application.services.PaymentApplicationService;
import vn.com.routex.hub.payment.service.application.services.merchant.PaymentOrchestrationService;
import vn.com.routex.hub.payment.service.domain.payment.PaymentMethod;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.ApiRequestUtils;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.ApiResultFactory;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.HttpUtils;
import vn.com.routex.hub.payment.service.interfaces.model.base.BaseRequest;
import vn.com.routex.hub.payment.service.interfaces.model.payment.CheckoutResponse;
import vn.com.routex.hub.payment.service.interfaces.model.payment.CreatePaymentSessionRequest;
import vn.com.routex.hub.payment.service.interfaces.model.payment.CreatePaymentSessionResponse;
import vn.com.routex.hub.payment.service.interfaces.model.payment.GetPaymentUrlResponse;

import java.math.BigDecimal;

import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.API_PATH;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.API_VERSION;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.CHECKOUT_PATH;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.GET_PAYMENT_URL;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.PAYMENT_PATH;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.PAY_SESSION_PATH;

@RequestMapping(API_PATH + API_VERSION + PAYMENT_PATH)
@RestController
@RequiredArgsConstructor
public class PaymentOrchestrationController {

    private final PaymentApplicationService paymentApplicationService;
    private final PaymentOrchestrationService paymentOrchestrationService;
    private final ApiResultFactory apiResultFactory;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder,  WebRequest webRequest) {
        webDataBinder.setDisallowedFields("requestId", "requestDateTime", "channel", "data");
    }

    @GetMapping(GET_PAYMENT_URL)
    public ResponseEntity<GetPaymentUrlResponse> getPaymentUrl(
            @RequestParam String bookingCode,
            @RequestParam PaymentMethod method,
            HttpServletRequest servletRequest
    ) {

        BaseRequest request = ApiRequestUtils.getBaseRequestOrDefault(servletRequest);

        GetPaymentUrlResult result = paymentOrchestrationService.getPaymentUrl(
                GetPaymentUrlCommand.builder()
                        .context(HttpUtils.toContext(request))
                        .bookingCode(bookingCode)
                        .method(method)
                        .build());


        GetPaymentUrlResponse response = GetPaymentUrlResponse.builder()
                .requestId(request.getRequestId())
                .requestDateTime(request.getRequestDateTime())
                .channel(request.getChannel())
                .result(apiResultFactory.buildSuccess())
                .data(GetPaymentUrlResponse.GetPaymentResponseData.builder()
                        .bookingCode(result.bookingCode())
                        .amount(result.amount())
                        .qrCodeUrl(result.qrCodeUrl())
                        .paymentUrl(result.paymentUrl())
                        .deeplink(result.deeplink())
                        .expiredTime(result.expiredTime())
                        .build())
                .build();



        return HttpUtils.buildResponse(request, response);
    }
    @PostMapping(PAY_SESSION_PATH)
    public ResponseEntity<CreatePaymentSessionResponse> createPaymentSession(@Valid @RequestBody CreatePaymentSessionRequest createPaymentSessionRequest) {
        CreatePaymentSessionResult result = paymentApplicationService.createPaymentSession(
                CreatePaymentSessionCommand.builder()
                        .context(HttpUtils.toContext(createPaymentSessionRequest))
                        .bookingId(createPaymentSessionRequest.getData().getBookingId())
                        .customerId(createPaymentSessionRequest.getData().getCustomerId())
                        .build()
        );
        return HttpUtils.buildResponse(createPaymentSessionRequest, toCreatePaymentSessionResponse(result));
    }

    @GetMapping(CHECKOUT_PATH)
    public ResponseEntity<CheckoutResponse> checkout(
            @RequestParam String paymentId,
            @RequestParam String token,
            HttpServletRequest request) {
        BaseRequest baseRequest = ApiRequestUtils.getBaseRequestOrDefault(request);
        CheckoutResult result = paymentApplicationService.checkout(
                CheckoutCommand.builder()
                        .context(HttpUtils.toContext(baseRequest))
                        .paymentId(paymentId)
                        .token(token)
                        .build()
        );

        CheckoutResponse response = CheckoutResponse.builder()
                .result(result.result())
                .data(CheckoutResponse.CheckoutResponseData.builder()
                        .paymentId(result.paymentId())
                        .status(result.status())
                        .amount(result.amount())
                        .currency(result.currency())
                        .paidAt(result.paidAt())
                        .build())
                .build();
        return HttpUtils.buildResponse(baseRequest, response);
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
}
