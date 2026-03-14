package com.software.order_service.service.action;

import com.software.order_service.model.OrderEvent;
import com.software.order_service.model.OrderState;
import com.software.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Component
public class ProcessPaymentAction implements Action<OrderState, OrderEvent> {

    private final RestTemplate restTemplate;
    private final String paymentServiceUrl;
    private final OrderService orderService; // <--- Add this

    // Inject OrderService with @Lazy to avoid Circular Dependencies
    public ProcessPaymentAction(RestTemplate restTemplate,
            @Value("${payment.service.url}") String paymentServiceUrl,
            @Lazy OrderService orderService) {
        this.restTemplate = restTemplate;
        this.paymentServiceUrl = paymentServiceUrl;
        this.orderService = orderService;
    }

    @Override
    public void execute(StateContext<OrderState, OrderEvent> context) {
        String orderId = (String) context.getMessageHeader("orderId");

        try {
            System.out.println("Calling Payment Service for Order: " + orderId);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    paymentServiceUrl + "/api/payments?orderId=" + orderId, null, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                sendEvent(OrderEvent.PAYMENT_SUCCESS, orderId);
            } else {
                sendEvent(OrderEvent.PAYMENT_FAILED, orderId);
            }
        } catch (Exception e) {
            System.out.println("Payment Service failed/unavailable for Order: " + orderId);
            sendEvent(OrderEvent.PAYMENT_FAILED, orderId);
        }
    }

    // Helper: Use OrderService instead of the transient StateMachine context
    private void sendEvent(OrderEvent event, String orderId) {
        // A short 500ms delay ensures the database is unlocked before firing
        Mono.delay(Duration.ofMillis(500)).subscribe(t -> {
            System.out.println("🚀 Triggering OrderService for event: " + event + " on Order: " + orderId);
            orderService.sendEvent(orderId, event); // <--- BOOM. Reliable event firing.
        });
    }
}