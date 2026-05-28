package vn.com.routex.hub.payment.service.interfaces.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import vn.com.go.routex.identity.security.log.SystemLog;
import vn.com.routex.hub.payment.service.application.command.common.RequestContext;
import vn.com.routex.hub.payment.service.application.services.PaymentContextQueryService;
import vn.com.routex.hub.payment.service.domain.payment.model.PaymentAggregate;
import vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ErrorConstant;
import vn.com.routex.hub.payment.service.infrastructure.persistence.exception.BusinessException;
import vn.com.routex.hub.grpc.PaymentGrpcServiceGrpc;
import vn.com.routex.hub.grpc.FetchPaymentContextRequest;
import vn.com.routex.hub.grpc.FetchPaymentContextResponse;

@GrpcService
@RequiredArgsConstructor
public class PaymentGrpcServiceImpl extends PaymentGrpcServiceGrpc.PaymentGrpcServiceImplBase {

    private final PaymentContextQueryService paymentContextQueryService;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    @Override
    public void fetchPaymentContext(FetchPaymentContextRequest request,
                                    StreamObserver<FetchPaymentContextResponse> responseObserver) {
        sLog.info("[GRPC] Received fetchPaymentContext request for bookingCode: {}", request.getBookingCode());
        try {
            RequestContext context = RequestContext.builder()
                    .requestId(request.getContext().getRequestId())
                    .requestDateTime(request.getContext().getRequestDateTime())
                    .channel(request.getContext().getChannel())
                    .build();

            PaymentAggregate payment = paymentContextQueryService.getPaymentContext(request.getBookingCode(), context);

            FetchPaymentContextResponse response = FetchPaymentContextResponse.newBuilder()
                    .setPaymentId(payment.getId() != null ? payment.getId() : "")
                    .setBookingCode(payment.getBookingCode() != null ? payment.getBookingCode() : "")
                    .setPaymentStatus(payment.getStatus() != null ? payment.getStatus().name() : "")
                    .setPaidAt(payment.getPaidAt() != null ? payment.getPaidAt().toString() : "")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            sLog.error("[GRPC] Error fetching payment context: {}", ex.getMessage(), ex);
            handleException(ex, responseObserver);
        }
    }

    private void handleException(Throwable ex, StreamObserver<?> responseObserver) {
        if (ex instanceof BusinessException businessEx) {
            String code = businessEx.getResult() != null ? businessEx.getResult().getResponseCode() : "99";
            String desc = businessEx.getResult() != null ? businessEx.getResult().getDescription() : ex.getMessage();
            Status status = Status.INTERNAL;
            if (ErrorConstant.RECORD_NOT_FOUND.equals(code)) {
                status = Status.NOT_FOUND;
            }
            responseObserver.onError(status.withDescription(desc).asRuntimeException());
        } else {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException());
        }
    }
}
