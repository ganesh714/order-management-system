package com.software.order_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import com.software.order_service.config.OrderStateChangeInterceptor;
import com.software.order_service.model.Order;
import com.software.order_service.model.OrderEvent;
import com.software.order_service.model.OrderState;
import com.software.order_service.repository.OrderRepository;

import reactor.core.publisher.Mono;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private StateMachineFactory<OrderState, OrderEvent> stateMachineFactory;
    @Autowired
    private OrderStateChangeInterceptor orderStateChangeInterceptor;

    public Order createOrder(Order order) {
        order.setStatus(OrderState.ORDER_CREATED);
        Order savedOrder = orderRepository.save(order);

        sendEvent(savedOrder.getOrderId(), OrderEvent.CREATE_ORDER);

        return savedOrder;
    }

    public Order getOrderByID(String id) {
        return orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public void sendEvent(String orderId, OrderEvent event) {
        StateMachine<OrderState, OrderEvent> sm = build(orderId);

        Message<OrderEvent> msg = MessageBuilder.withPayload(event)
                .setHeader("orderId", orderId)
                .build();

        sm.sendEvent(Mono.just(msg)).subscribe();
    }

    private StateMachine<OrderState, OrderEvent> build(String orderId) {
        Order order = getOrderByID(orderId);
        StateMachine<OrderState, OrderEvent> sm = stateMachineFactory.getStateMachine(orderId);

        sm.stopReactively().block();

        sm.getStateMachineAccessor().doWithAllRegions(sma -> {
            sma.addStateMachineInterceptor(orderStateChangeInterceptor);
            sma.resetStateMachineReactively(new DefaultStateMachineContext<>(order.getStatus(), null, null, null))
                    .block();
        });

        sm.startReactively().block();
        return sm;

    }

    public void updateOrderState(String id, OrderState state) {
        Order order = getOrderByID(id);
        order.setStatus(state);
        orderRepository.save(order);
    }
}
