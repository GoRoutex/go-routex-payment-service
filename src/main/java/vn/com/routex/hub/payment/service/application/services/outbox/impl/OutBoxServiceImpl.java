package vn.com.routex.hub.payment.service.application.services.outbox.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.go.routex.identity.security.log.SystemLog;
import vn.com.routex.hub.payment.service.application.services.outbox.OutBoxService;
import vn.com.routex.hub.payment.service.domain.outbox.OutBoxEventStatus;
import vn.com.routex.hub.payment.service.domain.outbox.model.OutBoxEvent;
import vn.com.routex.hub.payment.service.domain.outbox.port.OutBoxEventRepositoryPort;
import vn.com.routex.hub.payment.service.interfaces.model.base.BaseRequest;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutBoxServiceImpl implements OutBoxService {

    private final OutBoxEventRepositoryPort outBoxEventRepositoryPort;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());
    @Override
    @Transactional
    public void generateEvent(String aggregateId, String topic, String eventName, String eventKey, Object payload, BaseRequest context) {
        OutBoxEvent outboxEvent = OutBoxEvent.builder()
                .id(UUID.randomUUID().toString())
                .aggregateId(aggregateId)
                .topic(topic)
                .eventType(eventName)
                .eventKey(eventKey)
                .payload(Map.of("data", payload))
                .header(Map.of("context", context))
                .status(OutBoxEventStatus.PENDING)
                .retryCount(0)
                .availableAt(OffsetDateTime.now())
                .processedAt(null)
                .build();

        outBoxEventRepositoryPort.save(outboxEvent);
        sLog.info("[OUTBOX-EVENT] Outbox event generated and queued: AggregateId={} EventId={}",
                aggregateId, outboxEvent.getId());
    }
}
