package com.software.payment_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Value("${fail.on.order.id}")
    private String failOnOrderId;

    @PostMapping
    public ResponseEntity<String> processPayment(@RequestBody String orderId) {
        System.out.println("Received payment request for Order: " + orderId);

        // Simulate a deterministic failure for testing the Saga
        if (orderId.equals(failOnOrderId)) {
            System.err.println("Simulating Payment Failure for Order: " + orderId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Payment processing failed due to insufficient funds.");
        }

        System.out.println("Payment processed successfully for Order: " + orderId);
        return ResponseEntity.ok("Payment successful.");
    }
    
    @DeleteMapping("payments")
    public ResponseEntity<String> refundPayment(@RequestParam String orderId){
    	System.out.println("COMPENSATION: Received refund request for Order: " + orderId);
    	return ResponseEntity.ok("Payment refunded successfully.");
    }
}
