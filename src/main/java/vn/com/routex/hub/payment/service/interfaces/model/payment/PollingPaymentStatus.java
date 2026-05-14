package vn.com.routex.hub.payment.service.interfaces.model.payment;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.routex.hub.payment.service.domain.booking.PaymentStatus;
import vn.com.routex.hub.payment.service.interfaces.model.base.BaseResponse;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PollingPaymentStatus extends BaseResponse<PollingPaymentStatus.PollingPaymentStatusData> {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class PollingPaymentStatusData {
        private String bookingCode;
        private PaymentStatus status;
        private BigDecimal amount;
    }
}
