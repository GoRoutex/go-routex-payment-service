package vn.com.routex.hub.payment.service.controller.model.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.routex.hub.payment.service.controller.model.base.BaseResponse;
import vn.com.routex.hub.payment.service.domain.booking.BookingStatus;
import vn.com.routex.hub.payment.service.domain.booking.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CheckoutResponse extends BaseResponse<CheckoutResponse.CheckoutResponseData> {


    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    public static class CheckoutResponseData {
        private String paymentId;
        private PaymentStatus status;
        private BigDecimal amount;
        private String currency;
        private OffsetDateTime paidAt;
    }
}

