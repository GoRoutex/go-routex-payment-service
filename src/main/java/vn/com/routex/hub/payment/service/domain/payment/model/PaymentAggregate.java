package vn.com.routex.hub.payment.service.domain.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.routex.hub.payment.service.domain.booking.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAggregate {
    private String id;
    private String bookingId;
    private String code;
    private String method;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String checkoutUrl;
    private String paymentToken;
    private OffsetDateTime paidAt;
    private OffsetDateTime expiredAt;
    private OffsetDateTime failedAt;
    private String failureReason;
    private String description;
    private String createdBy;
    private OffsetDateTime createdAt;
    private String updatedBy;
    private OffsetDateTime updatedAt;

    public boolean isReusablePendingPayment(OffsetDateTime now) {
        return PaymentStatus.PENDING.equals(status) && expiredAt != null && !expiredAt.isBefore(now);
    }

    public void markPaid(OffsetDateTime now) {
        paidAt = now;
        status = PaymentStatus.PAID;
        updatedAt = now;
        failureReason = null;
    }

    public void markFailed(OffsetDateTime now, String reason) {
        status = PaymentStatus.FAILED;
        failedAt = now;
        updatedAt = now;
        failureReason = reason;
    }
}
