package com.software.order_service.config;

import java.util.EnumSet;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import com.software.order_service.model.OrderEvent;
import com.software.order_service.model.OrderState;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<OrderState, OrderEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<OrderState, OrderEvent> states) throws Exception {
        states.withStates()
                .initial(OrderState.ORDER_CREATED)
                .states(EnumSet.allOf(OrderState.class))
                .end(OrderState.ORDER_CREATED)
                .end(OrderState.ORDER_FAILED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions) throws Exception {
        transitions
                // 1. Start the process
                .withExternal().source(OrderState.ORDER_CREATED).target(OrderState.PAYMENT_PENDING)
                .event(OrderEvent.CREATE_ORDER)
                .and()
                // 2. Payment Success Path
                .withExternal().source(OrderState.PAYMENT_PENDING).target(OrderState.PAYMENT_COMPLETED)
                .event(OrderEvent.PAYMENT_SUCCESS)
                .and()
                // 3. Payment Failure Path
                .withExternal().source(OrderState.PAYMENT_PENDING).target(OrderState.ORDER_FAILED)
                .event(OrderEvent.PAYMENT_FAILED)
                .and()
                // 4. Inventory Success Path
                .withExternal().source(OrderState.PAYMENT_COMPLETED).target(OrderState.ORDER_COMPLETED)
                .event(OrderEvent.INVENTORY_SUCCESS)
                .and()
                // 5. Inventory Failure Path (Triggers Compensating Transaction later)
                .withExternal().source(OrderState.PAYMENT_COMPLETED).target(OrderState.ORDER_FAILED)
                .event(OrderEvent.INVENTORY_FAILED);
    }
}
