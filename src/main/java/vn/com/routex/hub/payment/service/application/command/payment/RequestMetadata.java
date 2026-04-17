package vn.com.routex.hub.payment.service.application.command.payment;

import lombok.Builder;

@Builder
public record RequestMetadata(
        String requestId,
        String requestDateTime,
        String channel
) {
}
