package vn.com.routex.hub.payment.service.infrastructure.persistence.exception;

import lombok.EqualsAndHashCode;
import vn.com.routex.hub.payment.service.controller.model.result.ApiResult;

@EqualsAndHashCode(callSuper = true)
public class BusinessException extends BaseException {

    public BusinessException(String requestId, String requestDateTime, String channel, ApiResult result) {
        super(requestId, requestDateTime, channel, result);
    }

    public BusinessException(ApiResult result) { super(result); }
}
