package vn.com.routex.hub.payment.service.application.command.common;

import lombok.Builder;

@Builder
public record PageContext(
        String pageSize,
        String pageNumber
) {
}
