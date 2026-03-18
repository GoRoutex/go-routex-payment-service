package vn.com.routex.hub.payment.service.infrastructure.persistence.utils;


import lombok.experimental.UtilityClass;
import vn.com.routex.hub.payment.service.controller.model.result.ApiResult;

@UtilityClass
public class ExceptionUtils {

    public ApiResult buildResultResponse(String responseCode, String description) {
        return ApiResult
                .builder()
                .responseCode(responseCode)
                .description(description)
                .build();
    }
}
