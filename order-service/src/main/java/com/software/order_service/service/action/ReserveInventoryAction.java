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
public class ReserveInventoryAction implements Action<OrderState, OrderEvent> {

    private final RestTemplate restTemplate;
    private final String inventoryServiceUrl;

    public ReserveInventoryAction(RestTemplate restTemplate,
            @Value("${inventory.service.url}") String inventoryServiceUrl) {
        this.restTemplate = restTemplate;
        this.inventoryServiceUrl = inventoryServiceUrl;
    }

    @Override
    public void execute(StateContext<OrderState, OrderEvent> context) {
        String orderId = (String) context.getMessageHeader("orderId");

        try {
            System.out.println("Calling Inventory Service for Order: " + orderId);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    inventoryServiceUrl + "/api/inventory?orderId=" + orderId, null, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                sendEvent(context, OrderEvent.INVENTORY_SUCCESS, orderId);
            } else {
                sendEvent(context, OrderEvent.INVENTORY_FAILED, orderId);
            }
        } catch (Exception e) {
            System.out.println("Inventory Service failed/unavailable for Order: " + orderId);
            sendEvent(context, OrderEvent.INVENTORY_FAILED, orderId);
        }
    }

    private void sendEvent(StateContext<OrderState, OrderEvent> context, OrderEvent event, String orderId) {
        context.getStateMachine().sendEvent(Mono.just(MessageBuilder.withPayload(event)
                .setHeader("orderId", orderId).build())).subscribe();
    }
}