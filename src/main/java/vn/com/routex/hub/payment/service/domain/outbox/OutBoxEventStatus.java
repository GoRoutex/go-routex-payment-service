package vn.com.routex.hub.payment.service.domain.outbox;

public enum OutBoxEventStatus {
    PENDING,
    PROCESSED,
    COMPLETED,
    FAILED
}
