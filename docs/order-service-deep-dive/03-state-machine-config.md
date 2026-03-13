# State Machine Configuration Deep Dive

This is the brain of the Saga pattern. It strictly maps out the states an order can exist in and the rules for moving between them.

## Key Spring State Machine Classes
* **`@EnableStateMachineFactory`**: We do NOT want one global state machine for the whole app. We use a *Factory* because every single order needs its own unique, isolated State Machine instance to track its specific progress.
* **`EnumStateMachineConfigurerAdapter`**: A Spring helper class that allows us to configure the states and transitions using our `OrderState` and `OrderEvent` enums.

## Deep Dive: The Database Interceptor
```java
@Component
public class OrderStateChangeInterceptor extends StateMachineInterceptorAdapter<OrderState, OrderEvent>
```

State machines run entirely in the server's RAM. If the server restarts, the machine forgets where it was.
To fix this, we created an Interceptor.

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

## Integrating RestTemplate
To allow our Actions to actually talk to the other microservices (Payment and Inventory), we configured a `RestTemplate` (a synchronous HTTP client provided by Spring).
* **`RestTemplateConfig`**: Creates the `RestTemplate` Bean.
* **Injection**: We inject `RestTemplate` into our Action classes, along with the external service URLs we defined in `application.properties` using `@Value("${payment.service.url}")`.
* **Execution**: During the Action's `execute()` method, we make an HTTP POST or DELETE call and inspect the response. Depending on whether the response is a success (200 OK) or failure (500 Error), we shoot a new `OrderEvent` (like `PAYMENT_SUCCESS` or `PAYMENT_FAILED`) back into the State Machine to push the Saga forward or trigger a rollback.
