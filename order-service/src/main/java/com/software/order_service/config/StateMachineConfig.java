package com.software.order_service.config;

import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;

import com.software.order_service.model.OrderEvent;
import com.software.order_service.model.OrderState;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<OrderState, OrderEvent> {

    @Autowired
    private Action<OrderState, OrderEvent> processPaymentAction;
    @Autowired
    private Action<OrderState, OrderEvent> reserveInventoryAction;
    @Autowired
    private Action<OrderState, OrderEvent> compensatePaymentAction;

    @Autowired
    private StateMachineRuntimePersister<OrderState, OrderEvent, String> stateMachineRuntimePersister;

    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderState, OrderEvent> config) throws Exception {
        config
            .withPersistence()
            .runtimePersister(stateMachineRuntimePersister);
    }

    @Override
    public void configure(StateMachineStateConfigurer<OrderState, OrderEvent> states) throws Exception {
        states.withStates()
                .initial(OrderState.ORDER_CREATED)
                .states(EnumSet.allOf(OrderState.class))
                .end(OrderState.ORDER_COMPLETED)
                .end(OrderState.ORDER_FAILED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions) throws Exception {
        transitions
                // 1. Start the process AND trigger the Payment Action
                .withExternal().source(OrderState.ORDER_CREATED).target(OrderState.PAYMENT_PENDING)
                .event(OrderEvent.CREATE_ORDER)
                .action(processPaymentAction) // <--- ATTACHED
                .and()
                // 2. Payment Success -> Trigger Inventory Action
                .withExternal().source(OrderState.PAYMENT_PENDING).target(OrderState.PAYMENT_COMPLETED)
                .event(OrderEvent.PAYMENT_SUCCESS)
                .action(reserveInventoryAction) // <--- ATTACHED
                .and()
                // 3. Payment Failure -> End in Failure
                .withExternal().source(OrderState.PAYMENT_PENDING).target(OrderState.ORDER_FAILED)
                .event(OrderEvent.PAYMENT_FAILED)
                .and()
                // 4. Inventory Success -> End in Success
                .withExternal().source(OrderState.PAYMENT_COMPLETED).target(OrderState.ORDER_COMPLETED)
                .event(OrderEvent.INVENTORY_SUCCESS)
                .and()
                // 5. Inventory Failure -> End in Failure AND trigger Compensating Action (Undo
                // Payment)
                .withExternal().source(OrderState.PAYMENT_COMPLETED).target(OrderState.ORDER_FAILED)
                .event(OrderEvent.INVENTORY_FAILED)
                .action(compensatePaymentAction);
    }
}
