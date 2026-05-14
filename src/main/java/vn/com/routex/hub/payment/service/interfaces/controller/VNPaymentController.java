package vn.com.routex.hub.payment.service.interfaces.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.routex.hub.payment.service.application.services.VNPayService;
import vn.com.routex.hub.payment.service.interfaces.model.vnpay.VNPayIpnResponse;

import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.API_PATH;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.API_VERSION;
import static vn.com.routex.hub.payment.service.infrastructure.persistence.constant.ApiConstant.PAYMENT_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + PAYMENT_PATH)
@RequiredArgsConstructor
public class VNPaymentController {

    private final VNPayService vnPayService;

    @GetMapping("/vnpay-ipn")
    public ResponseEntity<VNPayIpnResponse> vnpayIpn(HttpServletRequest request) {
        return ResponseEntity.ok(vnPayService.processIpn(request));
    }

}
