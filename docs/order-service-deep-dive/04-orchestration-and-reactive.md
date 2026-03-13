# Orchestration & Reactive Programming Deep Dive

The `OrderService` fuses the database and the State Machine together. It also handles the complexities of Spring State Machine 3.x, which introduced Reactive Programming.

## The Reactive Shift (Spring SM 3.x)
Older versions of Spring State Machine blocked the main thread while processing (Synchronous). Version 3.x uses **Project Reactor** to process state changes asynchronously.
* **`Mono`**: A reactive container that holds 0 or 1 item. We must wrap our events in a `Mono` before sending them to the state machine.
* **`.subscribe()`**: Reactive code is "lazy". Calling `sendEvent()` does nothing until someone subscribes. `.subscribe()` pulls the trigger and executes the flow in the background.
* **`.block()`**: Sometimes we *need* the code to pause. When building the machine, we use `.block()` to force the app to wait until the machine finishes resetting before moving on.

## Deep Dive: MessageBuilder
```java
Message<OrderEvent> msg = MessageBuilder.withPayload(event)
        .setHeader("orderId", orderId)
        .build();
```

We don't just send a raw `OrderEvent.CREATE_ORDER` into the machine. We use `MessageBuilder` to attach metadata (like an HTTP header). We attach the `orderId`. This is critical, because our `OrderStateChangeInterceptor` reads this exact header to know which order to update in PostgreSQL!

## Deep Dive: Rehydrating the Machine

The `build(String orderId)` method is the secret to Saga persistence.
Sagas span multiple API calls and take time. If we need to resume an order:

1. We pull the `Order` from PostgreSQL.
2. We ask the factory for a State Machine.
3. We **stop** it (`sm.stopReactively().block()`).
4. We **reset** its internal pointer to match the database (`order.getStatus()`).
5. We **start** it again.
This allows the system to seamlessly pause, wake up, and resume orchestrating long-running transactions.
