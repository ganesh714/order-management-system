package com.software.order_service.model;

public enum OrderEvent {
    CREATE_ORDER,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    INVENTORY_SUCCESS,
    INVENTORY_FAILED
}
