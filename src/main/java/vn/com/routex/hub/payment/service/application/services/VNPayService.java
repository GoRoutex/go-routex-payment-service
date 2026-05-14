package vn.com.routex.hub.payment.service.application.services;

import jakarta.servlet.http.HttpServletRequest;
import vn.com.routex.hub.payment.service.application.command.payment.GetPaymentUrlCommand;
import vn.com.routex.hub.payment.service.interfaces.model.vnpay.VNPayIpnResponse;

import java.io.UnsupportedEncodingException;

public interface VNPayService {

    String createPaymentUrl(GetPaymentUrlCommand command, String txnRef) throws UnsupportedEncodingException;

    VNPayIpnResponse processIpn(HttpServletRequest servletRequest);
}
