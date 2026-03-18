package vn.com.routex.hub.payment.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import vn.com.go.routex.identity.security.annotation.EnableIdentitySecurity;

@SpringBootApplication
@EnableIdentitySecurity
public class PaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceApplication.class, args);
	}

}
