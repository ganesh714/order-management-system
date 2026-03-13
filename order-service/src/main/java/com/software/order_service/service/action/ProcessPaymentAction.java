package com.software.order_service.action;

import com.software.order_service.model.OrderEvent;
import com.software.order_service.model.OrderState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

@Component
public class ProcessPaymentAction implements Action<OrderState, OrderEvent> {

    private final RestTemplate restTemplate;
    private final String paymentServiceUrl;

    // Inject the RestTemplate and the URL from your application.properties
    public ProcessPaymentAction(RestTemplate restTemplate,
            @Value("${payment.service.url}") String paymentServiceUrl) {
        this.restTemplate = restTemplate;
        this.paymentServiceUrl = paymentServiceUrl;
    }

    @Override
    public void execute(StateContext<OrderState, OrderEvent> context) {
        String orderId = (String) context.getMessageHeader("orderId");

        try {
            System.out.println("Calling Payment Service for Order: " + orderId);
            // Make a POST request to the Payment Service
            ResponseEntity<String> response = restTemplate.postForEntity(
                    paymentServiceUrl + "/api/payments?orderId=" + orderId, null, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                sendEvent(context, OrderEvent.PAYMENT_SUCCESS, orderId);
            } else {
                sendEvent(context, OrderEvent.PAYMENT_FAILED, orderId);
            }
        } catch (Exception e) {
            System.out.println("Payment Service failed/unavailable for Order: " + orderId);
            sendEvent(context, OrderEvent.PAYMENT_FAILED, orderId);
        }
    }

    // Helper to send the result back to the State Machine reactively
    private void sendEvent(StateContext<OrderState, OrderEvent> context, OrderEvent event, String orderId) {
        context.getStateMachine().sendEvent(Mono.just(MessageBuilder.withPayload(event)
                .setHeader("orderId", orderId).build())).subscribe();
    }
}