package vn.com.routex.hub.payment.service.application.service;

public interface QrService {
    String generateBase64Png(String qrContent, int width, int height);
}
