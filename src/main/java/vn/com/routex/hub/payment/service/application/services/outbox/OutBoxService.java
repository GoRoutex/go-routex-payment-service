package vn.com.routex.hub.payment.service.application.services.outbox;

import vn.com.routex.hub.payment.service.interfaces.model.base.BaseRequest;

public interface OutBoxService {
    void generateEvent(String aggregateId, String topic, String eventName, String eventKey, Object payload, BaseRequest context);
}
