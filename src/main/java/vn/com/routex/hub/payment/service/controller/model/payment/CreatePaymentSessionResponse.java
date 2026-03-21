package vn.com.routex.hub.payment.service.interfaces.models.payment;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.routex.hub.payment.service.interfaces.models.base.BaseResponse;
import vn.com.routex.hub.payment.service.domain.booking.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CreatePaymentSessionResponse extends BaseResponse<CreatePaymentSessionResponse.CreatePaymentSessionResponseData> {

    private CreatePaymentSessionResponseData data;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class CreatePaymentSessionResponseData {
        private String paymentId;
        private String bookingId;
        private BigDecimal amount;
        private String currency;
        private PaymentStatus status;
        private String qrContent;
        private String checkoutUrl;
        private OffsetDateTime expiresAt;
    }
}
