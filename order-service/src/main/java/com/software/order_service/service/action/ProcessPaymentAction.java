package com.software.order_service.service.action;

import com.software.order_service.model.OrderEvent;
import com.software.order_service.model.OrderState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
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

    // Safely queue the next event AFTER the current transition finishes
    private void sendEvent(StateContext<OrderState, OrderEvent> context, OrderEvent event, String orderId) {
        Mono.delay(Duration.ofMillis(200)).subscribe(t -> {
            System.out.println("🚀 Triggering internal event: " + event + " on Order: " + orderId);
            Message<OrderEvent> msg = MessageBuilder.withPayload(event).setHeader("orderId", orderId).build();
            context.getStateMachine().sendEvent(Mono.just(msg)).subscribe();
        });
    }
}