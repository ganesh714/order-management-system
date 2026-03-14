package com.software.order_service.config;

import com.software.order_service.model.Order;
import com.software.order_service.model.OrderEvent;
import com.software.order_service.model.OrderState;
import com.software.order_service.repository.OrderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderStateChangeInterceptor extends StateMachineInterceptorAdapter<OrderState, OrderEvent> {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public void preStateChange(State<OrderState, OrderEvent> state, Message<OrderEvent> message,
            Transition<OrderState, OrderEvent> transition, StateMachine<OrderState, OrderEvent> stateMachine,
            StateMachine<OrderState, OrderEvent> rootStateMachine) {

        // Extract the orderId we attached to the event message
        Optional.ofNullable(message)
                .flatMap(msg -> Optional.ofNullable((String) msg.getHeaders().getOrDefault("orderId", "")))
                .ifPresent(orderId -> {
                    // Find the order in the database
                    Order order = orderRepository.findById(orderId)
                            .orElseThrow(() -> new RuntimeException("Order Not Found"));

                    // Update the status to the new state
                    order.setStatus(state.getId());

                    // Save it back to PostgreSQL
                    orderRepository.save(order);

                    // Log it for observability (Requirement 11)
                    System.out.println(String.format("Saga for order %s transitioning from %s to %s on event %s.", 
                        orderId, 
                        transition.getSource().getId(), 
                        state.getId(), 
                        transition.getTrigger().getEvent()));
                });
    }
}