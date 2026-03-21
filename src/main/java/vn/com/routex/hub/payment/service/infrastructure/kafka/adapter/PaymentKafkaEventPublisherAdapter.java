package vn.com.routex.hub.payment.service.infrastructure.kafka.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import vn.com.routex.hub.payment.service.application.dto.payment.RequestMetadata;
import vn.com.routex.hub.payment.service.domain.payment.model.PaymentAggregate;
import vn.com.routex.hub.payment.service.domain.payment.port.PaymentEventPublisherPort;
import vn.com.routex.hub.payment.service.infrastructure.kafka.event.PaymentFailedEvent;
import vn.com.routex.hub.payment.service.infrastructure.kafka.event.PaymentSuccessEvent;
import vn.com.routex.hub.payment.service.infrastructure.kafka.model.KafkaEventMessage;
import vn.com.routex.hub.payment.service.infrastructure.persistence.utils.JsonUtils;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentKafkaEventPublisherAdapter implements PaymentEventPublisherPort {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topics.payment-success}")
    private String paymentSuccessTopic;

    @Value("${spring.kafka.topics.payment-failed}")
    private String paymentFailedTopic;

    @Value("${spring.kafka.events.payment-completed}")
    private String paymentCompletedEvent;

    @Value("${spring.kafka.events.payment-failed}")
    private String paymentFailedEvent;

    @Override
    public void publishPaymentSucceeded(RequestMetadata metadata, PaymentAggregate paymentAggregate) {
        PaymentSuccessEvent payload = PaymentSuccessEvent.builder()
                .paymentId(paymentAggregate.getId())
                .bookingId(paymentAggregate.getBookingId())
                .amount(paymentAggregate.getAmount())
                .currency(paymentAggregate.getCurrency())
                .status(paymentAggregate.getStatus())
                .paidAt(paymentAggregate.getPaidAt())
                .build();
        publish(metadata, paymentSuccessTopic, paymentCompletedEvent, paymentAggregate.getId(), payload);
    }

    @Override
    public void publishPaymentFailed(RequestMetadata metadata, PaymentAggregate paymentAggregate, String reason) {
        PaymentFailedEvent payload = PaymentFailedEvent.builder()
                .paymentId(paymentAggregate.getId())
                .bookingId(paymentAggregate.getBookingId())
                .status(paymentAggregate.getStatus())
                .reason(reason)
                .build();
        publish(metadata, paymentFailedTopic, paymentFailedEvent, paymentAggregate.getId(), payload);
    }

    private void publish(
            RequestMetadata metadata,
            String topicName,
            String eventName,
            String aggregateId,
            Object payload
    ) {
        try {
            KafkaEventMessage<Object> message = KafkaEventMessage.builder()
                    .requestId(metadata.requestId())
                    .requestDateTime(metadata.requestDateTime())
                    .channel(metadata.channel())
                    .eventId(UUID.randomUUID().toString())
                    .eventName(eventName)
                    .aggregateId(aggregateId)
                    .source("payment-service")
                    .version(1)
                    .occurredAt(OffsetDateTime.now())
                    .data(payload)
                    .build();
            kafkaTemplate.send(topicName, aggregateId, JsonUtils.parseToJsonStr(message));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Kafka publish failed", ex);
        }
    }
}
