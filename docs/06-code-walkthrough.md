# Common Annotations, Classes, and Functions

This guide explains the "magic" behind the Spring and Spring State Machine code used in this project.

## 1. Spring Boot Annotations

*   **`@Component`**: Tells Spring that this class is a "Bean" and should be managed by the Spring Container. This allows us to use `@Autowired` or constructor injection to use these classes elsewhere.
*   **`@Value("${...}")`**: Inject values from `application.properties` directly into variables. For example, `@Value("${payment.service.url}")` gives us the URL for the payment microservice.
*   **`@Lazy`**: Normally, Spring creates all beans as soon as the app starts. `@Lazy` tells Spring "Don't create this bean until someone actually asks for it." This is used to break **Circular Dependencies** (e.g., if Class A needs Class B and Class B needs Class A).
*   **`@Configuration`**: Marks a class as a source of bean definitions. We use this for `StateMachineConfig` and `RestTemplateConfig`.
*   **`@Bean`**: Used inside a `@Configuration` class to define a specific object (like `RestTemplate`) that should be available for injection throughout the app.

## 2. Spring State Machine Specifics

*   **`@EnableStateMachineFactory`**: Instead of a "Singleton" State Machine (one for everyone), this creates a factory. We give the factory an `orderId`, and it gives us a fresh `StateMachine` instance dedicated only to that order.
*   **`StateMachine<S, E>`**: The interface representing the machine. `S` is the State enum (`OrderState`) and `E` is the Event enum (`OrderEvent`).
*   **`StateContext<S, E>`**: A "context object" passed into Interceptors and Actions. It contains everything about the current transition: the machine itself, the message (with headers), and the source/target states.
*   **`Action<S, E>`**: An interface we implement to run code *during* a state transition. Our `execute(StateContext context)` method is where the real work (like calling a REST API) happens.
*   **`StateMachineInterceptor`**: A "watcher" that sits on the side of the machine. We use it to intercept state changes right before they happen (`preStateChange`) so we can save the new state to our PostgreSQL database.

## 3. Reactive Programming (Project Reactor)

*   **`Mono<T>`**: Think of this as a "Promise" that will eventually return a single value of type `T`.
*   **`.subscribe()`**: In reactive programming, nothing happens until you subscribe. When we call `sm.sendEvent(...).subscribe()`, we are telling the machine to "go ahead and process this event in the background."
*   **`.block()`**: Forces the current thread to wait until the `Mono` completes. We use `startReactively().block()` to ensure the state machine is fully "awake" before we try to use it.
*   **`Mono.delay(Duration)`**: Creates a small pause in the reactive flow. We use this to prevent race conditions between the database save and the next state machine event.

## 4. Helpful Functions

*   **`MessageBuilder`**: A utility to create a `Message` object. This is better than sending a raw event because it lets us attach **Headers** (like `orderId`), which our Interceptor uses to identify which database row to update.
*   **`stateMachineFactory.getStateMachine(id)`**: Fetches or creates a state machine instance for a specific ID.
*   **`sma.addStateMachineInterceptor(...)`**: Attaches our custom database-sync logic to the machine.
*   **`restTemplate.exchange(...)`**: A flexible way to make HTTP requests (GET, POST, DELETE) and get the full `ResponseEntity` (including headers and status codes) back.
