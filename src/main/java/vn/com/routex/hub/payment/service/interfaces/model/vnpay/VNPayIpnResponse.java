package vn.com.routex.hub.payment.service.interfaces.model.vnpay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VNPayIpnResponse {
    @JsonProperty("RspCode")
    private String RspCode;
    @JsonProperty("Message")
    private String Message;
}
