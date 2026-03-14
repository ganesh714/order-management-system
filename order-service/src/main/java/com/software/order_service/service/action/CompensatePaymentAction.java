package com.software.order_service.service.action;

import com.software.order_service.model.OrderEvent;
import com.software.order_service.model.OrderState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CompensatePaymentAction implements Action<OrderState, OrderEvent> {

    private final RestTemplate restTemplate;
    private final String paymentServiceUrl;

    public CompensatePaymentAction(RestTemplate restTemplate,
            @Value("${payment.service.url}") String paymentServiceUrl) {
        this.restTemplate = restTemplate;
        this.paymentServiceUrl = paymentServiceUrl;
    }

    @Override
    public void execute(StateContext<OrderState, OrderEvent> context) {
        String orderId = (String) context.getMessageHeader("orderId");

        try {
            System.out.println("Compensating Payment for Order: " + orderId);
            ResponseEntity<String> response = restTemplate.exchange(
                    paymentServiceUrl + "/api/payments/" + orderId + "/cancel", HttpMethod.DELETE, null, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Payment successfully refunded for Order: " + orderId);
            }
        } catch (Exception e) {
            System.out.println("Failed to compensate payment for Order: " + orderId);
        }
        // Ending in ORDER_FAILED, no further state machine events needed.
    }
}