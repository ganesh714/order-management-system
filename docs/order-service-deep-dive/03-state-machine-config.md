# State Machine Configuration Deep Dive

This is the brain of the Saga pattern. It strictly maps out the states an order can exist in and the rules for moving between them.

## Key Spring State Machine Classes
* **`@EnableStateMachineFactory`**: In a high-traffic system, we cannot use a single global state machine. This annotation allows Spring to create a **Factory** that can generate unique, isolated `StateMachine` instances for every single `orderId`. This ensures data isolation and thread safety across concurrent orders.
* **`EnumStateMachineConfigurerAdapter`**: A Spring helper class that allowing us to configure the states and transitions using our `OrderState` and `OrderEvent` enums.

## Deep Dive: The Database Interceptor
```java
@Component
public class OrderStateChangeInterceptor extends StateMachineInterceptorAdapter<OrderState, OrderEvent>
```

State machines run entirely in the server's RAM. If the server restarts, the machine forgets where it was. To fix this, we created an Interceptor.

* We override the `preStateChange` method.
* This acts as a hook that fires *just before* a state officially transitions.
* It pauses the machine, grabs the current state, saves that state to PostgreSQL (`order.setStatus(...)`), and then lets the machine continue.
* This guarantees our database is perfectly synchronized with the State Machine's memory.

## Deep Dive: Actions (Making the Machine "Do" Things)
State machines don't just passively change states; they can actively trigger code during a transition. We implement the `Action<OrderState, OrderEvent>` interface to define these behaviors.

* **`ProcessPaymentAction`**: Triggered when the machine moves to `PAYMENT_PENDING`. It reaches out to the Payment Service.
* **`ReserveInventoryAction`**: Triggered when the machine moves to `PAYMENT_COMPLETED`. It reaches out to the Inventory Service.
* **`CompensatePaymentAction`**: Triggered when inventory fails. It tells the Payment Service to issue a refund.

### Wiring Actions to Transitions
We wire these Actions directly into our transition rules in `StateMachineConfig.java`. For example:
```java
.withExternal().source(OrderState.ORDER_CREATED).target(OrderState.PAYMENT_PENDING)
.event(OrderEvent.CREATE_ORDER)
.action(processPaymentAction) // <--- The Action is fired right here!
```

## Integrating RestTemplate & Reliability
To allow our Actions to talk to other microservices, we use `RestTemplate`.

* **Injection**: We inject `RestTemplate` into our Action classes.
* **Reliability (The 200ms Delay)**: In high-concurrency environments, state transitions can sometimes happen faster than the database can unlock the `Order` record. To prevent race conditions, our Actions use:
  ```java
  Mono.delay(Duration.ofMillis(200)).subscribe(t -> { ... })
  ```
  This small delay ensures the `OrderStateChangeInterceptor` has finished saving the new state to the database before the next event (e.g., `PAYMENT_SUCCESS`) is fired back into the machine. 

### Decoupling circular dependencies
Previously, `ProcessPaymentAction` relied on `OrderService` to fire events. This created a **Circular Dependency**. We resolved this by firing events directly into the `StateMachine` available in the `StateContext`:
```java
context.getStateMachine().sendEvent(Mono.just(msg)).subscribe();
```
This is cleaner, more reactive, and adheres to the orchestrator pattern where the machine drives its own transitions.

