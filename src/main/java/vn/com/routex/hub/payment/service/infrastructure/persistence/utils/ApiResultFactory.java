package vn.com.routex.hub.payment.service.infrastructure.persistence.utils;


import org.springframework.stereotype.Component;
import vn.com.routex.hub.payment.service.interfaces.model.result.ApiResult;

import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.SUCCESS_CODE;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant.SUCCESS_MESSAGE;

@Component
public class ApiResultFactory {

    public ApiResult buildSuccess() {
        return ApiResult.builder()
                .responseCode(SUCCESS_CODE)
                .description(SUCCESS_MESSAGE)
                .build();
    }
}
