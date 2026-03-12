package com.software.order_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.software.order_service.model.Order;
import com.software.order_service.model.OrderState;
import com.software.order_service.repository.OrderRepository;

@Service
public class OrderService {
    @Autowired
    OrderRepository orderRepository;

    public Order createOrder(Order order) {
        order.setStatus(OrderState.ORDER_CREATED);
        Order savedOrder = orderRepository.save(order);
        // TODO : State macine
        return savedOrder;
    }

    public Order getOrderByID(String id) {
        return orderRepository.getReferenceById(id);
    }

    public void updateOrderState(String id, OrderState state) {
        Order order = getOrderByID(id);
        order.setStatus(state);
        orderRepository.save(order);
    }
}
