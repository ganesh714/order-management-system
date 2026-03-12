package com.software.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.software.order_service.model.Order;

public interface OrderRepository extends JpaRepository<Order, String> {

}
