package vn.com.routex.hub.payment.service.application.services;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import vn.com.routex.hub.payment.service.interfaces.model.vnpay.PaymentRequest;

import java.io.UnsupportedEncodingException;

public interface VNPayService {

    String createPaymentUrl(PaymentRequest request, HttpServletRequest servletRequest) throws UnsupportedEncodingException;
}
