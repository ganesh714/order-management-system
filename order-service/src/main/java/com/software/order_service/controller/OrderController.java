package com.software.order_service.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.software.order_service.dto.OrderRequest;
import com.software.order_service.dto.OrderResponse;
import com.software.order_service.model.Order;
import com.software.order_service.service.OrderService;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest orderRequest) {
        Order newOrder = new Order();
        newOrder.setCustomerId(orderRequest.getCustomerId());
        newOrder.setProductId(orderRequest.getProductId());
        newOrder.setQuantity(orderRequest.getQuantity());
        newOrder.setUnitPrice(orderRequest.getUnitPrice());

        Order savedOrder = orderService.createOrder(newOrder);

        OrderResponse response = new OrderResponse();
        response.setOrderId(savedOrder.getOrderId());
        response.setStatus(savedOrder.getStatus().name());
        response.setMessage("Order creation process initiated.");

        // Return a 202 Accepted status
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String id) {
        Order order = orderService.getOrderByID(id);

        BigDecimal totalAmount = order.getUnitPrice().multiply(new BigDecimal(order.getQuantity()));

        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setStatus(order.getStatus().name());
        response.setAmount(totalAmount);
        return ResponseEntity.ok(response);
    }
}
