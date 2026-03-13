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
