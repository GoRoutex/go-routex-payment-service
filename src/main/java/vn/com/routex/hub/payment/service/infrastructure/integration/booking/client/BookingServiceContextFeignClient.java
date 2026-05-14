package vn.com.routex.hub.payment.service.infrastructure.integration.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import vn.com.routex.hub.payment.service.infrastructure.integration.booking.dto.FetchBookingPaymentContextClientRequest;
import vn.com.routex.hub.payment.service.infrastructure.integration.booking.dto.FetchBookingPaymentContextClientResponse;
import vn.com.routex.hub.payment.service.infrastructure.integration.feign.config.ContextApiFeignConfig;

@FeignClient(
        name = "booking-service-context-client",
        url = "${clients.booking-service.base-url}",
        configuration = ContextApiFeignConfig.class
)
public interface BookingServiceContextFeignClient {

    @PostMapping("/api/v1/booking-service/payments/context")
    FetchBookingPaymentContextClientResponse fetchBookingPaymentContext(
            @RequestBody FetchBookingPaymentContextClientRequest request
    );
}
