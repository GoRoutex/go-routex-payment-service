package vn.com.routex.hub.payment.service.interfaces.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import vn.com.go.routex.identity.security.log.SystemLog;
import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlCommand;
import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlResult;
import vn.com.routex.hub.payment.service.application.services.merchant.PaymentOrchestrationService;
import vn.com.routex.hub.payment.service.domain.payment.PaymentMethod;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.ApiRequestUtils;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.ApiResultFactory;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.HttpUtils;
import vn.com.routex.hub.payment.service.interfaces.model.base.BaseRequest;
import vn.com.routex.hub.payment.service.interfaces.model.payment.GetPaymentUrlResponse;

import java.math.BigDecimal;

import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.API_PATH;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.API_VERSION;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.GET_PAYMENT_URL;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.PAYMENT_PATH;

@RequestMapping(API_PATH + API_VERSION + PAYMENT_PATH)
@RestController
@RequiredArgsConstructor
public class PaymentOrchestrationController {

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
            @RequestParam BigDecimal amount,
            HttpServletRequest servletRequest
    ) {

        BaseRequest request = ApiRequestUtils.getBaseRequestOrDefault(servletRequest);

        GetPaymentUrlCommand command = GetPaymentUrlCommand.builder()
                .context(HttpUtils.toContext(request))
                .amount(amount)
                .clientIp(resolveClientIp(servletRequest))
                .bookingCode(bookingCode)
                .method(method)
                .build();

        GetPaymentUrlResult result = paymentOrchestrationService.getPaymentUrl(command);


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

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-FORWARDED-FOR");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
