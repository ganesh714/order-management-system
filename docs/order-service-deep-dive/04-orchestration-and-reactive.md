# Orchestration & Reactive Programming Deep Dive

The `OrderService` fuses the database and the State Machine together. It also handles the complexities of Spring State Machine 4.x, which is built on a fully Reactive architecture.

## The Reactive Architecture (Spring SM 4.x)
Unlike older versions that blocked the main thread (Synchronous), Version 4.x uses **Project Reactor** to process state changes asynchronously. This improves the scalability of the application.

* **`Mono`**: A reactive container that holds 0 or 1 item. We must wrap our events in a `Mono` before sending them to the state machine.
* **`.subscribe()`**: Reactive code is "lazy". Calling `sendEvent()` does nothing until someone subscribes. `.subscribe()` pulls the trigger and executes the flow in the background.
* **`startReactively().block()`**: When starting the machine, we use `.block()` to force the application to wait until the machine is fully initialized before we attempt to send the first event.

## Deep Dive: MessageBuilder
```java
Message<OrderEvent> msg = MessageBuilder.withPayload(event)
        .setHeader("orderId", orderId)
        .build();
```

We don't just send a raw `OrderEvent` into the machine. We use `MessageBuilder` to attach metadata headers.
* **Why headers?**: Because Sagas can handle multiple orders at once, the `OrderStateChangeInterceptor` needs to know *which* order record in PostgreSQL to update. The "orderId" header is the primary key used to synchronize the database with the machine's memory.

## Deep Dive: Rehydrating the Machine

The `build(String orderId)` method is the secret to Saga persistence. Sagas span multiple API calls and take time. If we need to resume an order:

1. **Fetch**: We pull the `Order` entity from PostgreSQL.
2. **Retrieve**: We ask the `stateMachineFactory` for the specific machine associated with that `orderId`.
3. **Reset**: We use the `StateMachineAccessor` to manually set the machine's state to match what's in the database (`order.getStatus()`).
4. **Start**: We call `sm.startReactively().block()` to wake the machine up.

This allows the system to seamlessly pause, wake up, and resume orchestrating long-running transactions even if the server restarts.

