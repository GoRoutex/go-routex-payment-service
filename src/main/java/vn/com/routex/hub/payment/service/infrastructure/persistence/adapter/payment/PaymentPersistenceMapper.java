package vn.com.routex.hub.payment.service.infrastructure.persistence.adapter.payment;

import org.springframework.stereotype.Component;
import vn.com.routex.hub.payment.service.domain.payment.model.PaymentAggregate;
import vn.com.routex.hub.payment.service.infrastructure.persistence.jpa.payment.entity.PaymentJpaEntity;

@Component
public class PaymentPersistenceMapper {

    public PaymentAggregate toDomain(PaymentJpaEntity paymentJpaEntity) {
        return PaymentAggregate.builder()
                .id(paymentJpaEntity.getId())
                .bookingId(paymentJpaEntity.getBookingId())
                .code(paymentJpaEntity.getCode())
                .method(paymentJpaEntity.getMethod())
                .amount(paymentJpaEntity.getAmount())
                .currency(paymentJpaEntity.getCurrency())
                .status(paymentJpaEntity.getStatus())
                .checkoutUrl(paymentJpaEntity.getCheckoutUrl())
                .paymentToken(paymentJpaEntity.getPaymentToken())
                .paidAt(paymentJpaEntity.getPaidAt())
                .expiredAt(paymentJpaEntity.getExpiredAt())
                .failedAt(paymentJpaEntity.getFailedAt())
                .failureReason(paymentJpaEntity.getFailureReason())
                .description(paymentJpaEntity.getDescription())
                .createdBy(paymentJpaEntity.getCreatedBy())
                .createdAt(paymentJpaEntity.getCreatedAt())
                .updatedBy(paymentJpaEntity.getUpdatedBy())
                .updatedAt(paymentJpaEntity.getUpdatedAt())
                .build();
    }

    public PaymentJpaEntity toJpaEntity(PaymentAggregate paymentAggregate) {
        PaymentJpaEntity paymentJpaEntity = PaymentJpaEntity.builder()
                .id(paymentAggregate.getId())
                .bookingId(paymentAggregate.getBookingId())
                .code(paymentAggregate.getCode())
                .method(paymentAggregate.getMethod())
                .amount(paymentAggregate.getAmount())
                .currency(paymentAggregate.getCurrency())
                .status(paymentAggregate.getStatus())
                .checkoutUrl(paymentAggregate.getCheckoutUrl())
                .paymentToken(paymentAggregate.getPaymentToken())
                .paidAt(paymentAggregate.getPaidAt())
                .expiredAt(paymentAggregate.getExpiredAt())
                .failedAt(paymentAggregate.getFailedAt())
                .failureReason(paymentAggregate.getFailureReason())
                .description(paymentAggregate.getDescription())
                .build();
        paymentJpaEntity.setCreatedBy(paymentAggregate.getCreatedBy());
        paymentJpaEntity.setCreatedAt(paymentAggregate.getCreatedAt());
        paymentJpaEntity.setUpdatedBy(paymentAggregate.getUpdatedBy());
        paymentJpaEntity.setUpdatedAt(paymentAggregate.getUpdatedAt());
        return paymentJpaEntity;
    }
}
