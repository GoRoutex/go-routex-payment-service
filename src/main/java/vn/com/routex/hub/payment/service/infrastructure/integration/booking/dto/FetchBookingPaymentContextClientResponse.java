package vn.com.routex.hub.payment.service.infrastructure.integration.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FetchBookingPaymentContextClientResponse {

    private String requestId;
    private String requestDateTime;
    private String channel;
    private Object result;
    private FetchBookingPaymentContextClientResponseData data;
}
