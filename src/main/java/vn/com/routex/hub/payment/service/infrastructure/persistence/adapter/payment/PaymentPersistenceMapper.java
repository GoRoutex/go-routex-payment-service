package vn.com.routex.hub.payment.service.infrastructure.persistence.adapter.payment;

import org.springframework.stereotype.Component;
import vn.com.routex.hub.payment.service.domain.payment.model.PaymentAggregate;
import vn.com.routex.hub.payment.service.infrastructure.persistence.jpa.payment.entity.PaymentEntity;

@Component
public class PaymentPersistenceMapper {

    public PaymentAggregate toDomain(PaymentEntity paymentJpaEntity) {
        return PaymentAggregate.builder()
                .id(paymentJpaEntity.getId())
                .bookingCode(paymentJpaEntity.getBookingCode())
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

    public PaymentEntity toJpaEntity(PaymentAggregate paymentAggregate) {
        PaymentEntity paymentJpaEntity = PaymentEntity.builder()
                .id(paymentAggregate.getId())
                .bookingCode(paymentAggregate.getBookingCode())
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
