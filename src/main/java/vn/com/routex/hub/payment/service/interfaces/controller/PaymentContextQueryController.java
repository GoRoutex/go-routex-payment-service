package vn.com.routex.hub.payment.service.interfaces.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import vn.com.routex.hub.payment.service.application.command.common.RequestContext;
import vn.com.routex.hub.payment.service.application.services.PaymentContextQueryService;
import vn.com.routex.hub.payment.service.domain.payment.model.PaymentAggregate;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.ApiResultFactory;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.HttpUtils;
import vn.com.routex.hub.payment.service.interfaces.model.base.BaseRequest;
import vn.com.routex.hub.payment.service.interfaces.model.payment.FetchPaymentContextRequest;
import vn.com.routex.hub.payment.service.interfaces.model.payment.FetchPaymentContextResponse;

import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.API_PATH;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.API_VERSION;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.PAYMENT_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + PAYMENT_PATH + "/payments")
@RequiredArgsConstructor
public class PaymentContextQueryController {

    private final PaymentContextQueryService paymentContextQueryService;
    private final ApiResultFactory apiResultFactory;

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder, WebRequest webRequest) {
        webDataBinder.setDisallowedFields("requestId", "requestDateTime", "channel", "data");
    }

    @PostMapping("/context")
    public ResponseEntity<FetchPaymentContextResponse> fetchPaymentContext(
            @Valid @RequestBody FetchPaymentContextRequest request
    ) {
        PaymentAggregate payment = paymentContextQueryService.getPaymentContext(
                request.getData().getBookingCode(),
                toContext(request)
        );

        FetchPaymentContextResponse response = FetchPaymentContextResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(FetchPaymentContextResponse.Data.builder()
                        .paymentId(payment.getId())
                        .bookingCode(payment.getBookingCode())
                        .paymentStatus(payment.getStatus().name())
                        .paidAt(payment.getPaidAt())
                        .build())
                .build();

        return HttpUtils.buildResponse(request, response);
    }

    private RequestContext toContext(BaseRequest request) {
        return RequestContext.builder()
                .requestId(request.getRequestId())
                .requestDateTime(request.getRequestDateTime())
                .channel(request.getChannel())
                .build();
    }
}
