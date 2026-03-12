# Order Service (The Orchestrator)

## Role in the Architecture
The Order Service acts as the central **Orchestrator** (the Manager) for the Saga pattern. It directs the lifecycle of an order by coordinating with the other worker services.
It is the only service in this specific setup that has a PostgreSQL database to reliably persist order states.

## The Order Entity
The `Order` entity tracks the order's core fields (like price and items) and its current state.
The status of the order uses `@Enumerated(EnumType.STRING)` to persist the exact string value of the Enum instead of its ordinal integer. This keeps the database readable and avoids issues if Enum values are rearranged in the code.

## Interactions with Spring State Machine
The `OrderService` interacts with the Spring State Machine to progress the states. Instead of updating states directly, it triggers events that the State Machine listens to. 

## StateMachineInterceptor
A `StateMachineInterceptor` is used to watch the state machine. Its primary job is to observe state transitions and intercept the changes to update the order state in the PostgreSQL database reliably, ensuring the state machine and the database stay strictly synchronized.
