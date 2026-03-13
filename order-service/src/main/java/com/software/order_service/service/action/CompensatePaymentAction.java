package com.software.order_service.action;

import com.software.order_service.model.OrderEvent;
import com.software.order_service.model.OrderState;
import org.springframework.beans.factory.annotation.Value;
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
        System.out.println("COMPENSATING TRANSACTION: Refunding payment for Order: " + orderId);

        try {
            // Using DELETE method to represent a cancellation/refund
            restTemplate.delete(paymentServiceUrl + "/api/payments?orderId=" + orderId);
            System.out.println("Refund processed successfully.");
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to refund payment for Order: " + orderId);
        }
    }
}