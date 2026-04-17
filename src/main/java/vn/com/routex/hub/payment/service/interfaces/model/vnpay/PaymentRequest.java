package vn.com.routex.hub.payment.service.interfaces.model.vnpay;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.routex.hub.payment.service.interfaces.model.base.BaseRequest;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class PaymentRequest extends BaseRequest {

    @Valid
    @NotNull
    private PaymentRequestData data;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class PaymentRequestData {
        private String referenceNo;
        private String creator;
        private String bankCode;
        private String language;
        private BigDecimal amount;
    }
}
