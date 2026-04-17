package vn.com.routex.hub.payment.service.interfaces.model.vnpay;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.routex.hub.payment.service.interfaces.model.base.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PaymentResponse extends BaseResponse<PaymentResponse.PaymentResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    @AllArgsConstructor
    public static class PaymentResponseData {
        private String paymentUrl;
    }
}
