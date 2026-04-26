package vn.com.routex.hub.payment.service.application.services.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.com.routex.hub.payment.service.application.command.payment.RequestMetadata;
import vn.com.routex.hub.payment.service.application.services.VNPayService;
import vn.com.routex.hub.payment.service.domain.booking.PaymentStatus;
import vn.com.routex.hub.payment.service.domain.payment.model.PaymentAggregate;
import vn.com.routex.hub.payment.service.domain.payment.port.PaymentEventPublisherPort;
import vn.com.routex.hub.payment.service.domain.payment.port.PaymentRepositoryPort;
import vn.com.routex.hub.payment.service.infrastructure.integration.constant.VNPayConstant;
import vn.com.routex.hub.payment.service.infrastructure.integration.utils.VNPayUtils;
import vn.com.routex.hub.payment.service.interfaces.model.vnpay.PaymentRequest;
import vn.com.routex.hub.payment.service.interfaces.model.vnpay.VNPayIpnResponse;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


@RequiredArgsConstructor
@Service
public class VNPayServiceImpl implements VNPayService {

    private final VNPayUtils vnPayUtils;
    private final PaymentRepositoryPort paymentRepositoryPort;
    private final PaymentEventPublisherPort paymentEventPublisherPort;

    @Override
    public String createPaymentUrl(PaymentRequest request, HttpServletRequest servletRequest) throws UnsupportedEncodingException {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        BigDecimal amount = request.getData().getAmount().multiply(BigDecimal.valueOf(100));

        String bankCode = request.getData().getBankCode();

        String vnp_TxnRef = hasText(request.getData().getReferenceNo())
                ? request.getData().getReferenceNo().trim()
                : vnPayUtils.getRandomNumber(8);
        String vnp_IpAddr = vnPayUtils.getIpAddress(servletRequest);

        String vnp_TmnCode = VNPayConstant.vnp_TMNCODE;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);

        String locate = request.getData().getLanguage();
        if (locate != null && !locate.isEmpty()) {
            vnp_Params.put("vnp_Locale", locate);
        } else {
            vnp_Params.put("vnp_Locale", "vn");
        }
        vnp_Params.put("vnp_ReturnUrl", VNPayConstant.vnp_RETURNURL);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {

            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);

            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                //Build hash data
                hashData.append(fieldName)
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = vnPayUtils.hmacSHA512(VNPayConstant.SECRET_KEY, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return VNPayConstant.vnp_PAYURL + "?" + queryUrl;
    }

    @Override
    public VNPayIpnResponse processIpn(HttpServletRequest servletRequest) {
        try {
            Map<String, String> fields = collectEncodedFields(servletRequest);
            String vnpSecureHash = servletRequest.getParameter("vnp_SecureHash");

            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            String signValue = vnPayUtils.hashAllFields(fields);
            if (!signValue.equals(vnpSecureHash)) {
                return ipnResponse("97", "Invalid Checksum");
            }

            String txnRef = servletRequest.getParameter("vnp_TxnRef");
            if (!hasText(txnRef)) {
                return ipnResponse("01", "Order not Found");
            }

            PaymentAggregate payment = findPayment(txnRef.trim());
            if (payment == null) {
                return ipnResponse("01", "Order not Found");
            }

            if (!isValidAmount(payment, servletRequest.getParameter("vnp_Amount"))) {
                return ipnResponse("04", "Invalid Amount");
            }

            if (!PaymentStatus.PENDING.equals(payment.getStatus())) {
                return ipnResponse("02", "Order already confirmed");
            }

            String responseCode = servletRequest.getParameter("vnp_ResponseCode");
            if ("00".equals(responseCode)) {
                payment.markPaid(OffsetDateTime.now());
                PaymentAggregate savedPayment = paymentRepositoryPort.save(payment);
                paymentEventPublisherPort.publishPaymentSucceeded(buildMetadata(), savedPayment);
                return ipnResponse("00", "Confirm Success");
            }

            String failureReason = "VNPAY payment failed with response code: " + responseCode;
            payment.markFailed(OffsetDateTime.now(), failureReason);
            PaymentAggregate failedPayment = paymentRepositoryPort.save(payment);
            paymentEventPublisherPort.publishPaymentFailed(buildMetadata(), failedPayment, failureReason);
            return ipnResponse("00", "Confirm Success");
        } catch (Exception ex) {
            return ipnResponse("99", "Unknown error");
        }
    }

    private Map<String, String> collectEncodedFields(HttpServletRequest servletRequest) throws UnsupportedEncodingException {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = servletRequest.getParameterNames(); params.hasMoreElements(); ) {
            String paramName = params.nextElement();
            String paramValue = servletRequest.getParameter(paramName);
            if (paramValue == null || paramValue.isEmpty()) {
                continue;
            }
            fields.put(
                    URLEncoder.encode(paramName, StandardCharsets.US_ASCII),
                    URLEncoder.encode(paramValue, StandardCharsets.US_ASCII)
            );
        }
        return fields;
    }

    private PaymentAggregate findPayment(String txnRef) {
        return paymentRepositoryPort.findById(txnRef)
                .or(() -> paymentRepositoryPort.findByCode(txnRef))
                .orElse(null);
    }

    private boolean isValidAmount(PaymentAggregate payment, String amountParam) {
        if (!hasText(amountParam)) {
            return false;
        }
        try {
            BigDecimal returnedAmount = new BigDecimal(amountParam.trim());
            BigDecimal expectedAmount = payment.getAmount().multiply(BigDecimal.valueOf(100));
            return expectedAmount.compareTo(returnedAmount) == 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private RequestMetadata buildMetadata() {
        String now = OffsetDateTime.now().toString();
        return RequestMetadata.builder()
                .requestId("vnpay-ipn")
                .requestDateTime(now)
                .channel("VNPAY")
                .build();
    }

    private VNPayIpnResponse ipnResponse(String rspCode, String message) {
        return VNPayIpnResponse.builder()
                .RspCode(rspCode)
                .Message(message)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
