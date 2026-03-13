# Order Service (The Orchestrator)

The Order Service is the "Manager" of our Saga. It exposes REST APIs to the user, saves order data to PostgreSQL, and uses Spring State Machine to orchestrate the distributed transaction.

Because this service is complex, the documentation is split into specific deep dives. Please refer to the following guides to understand the architecture, code blocks, and Spring Boot classes used:

1. [API & Controller Layer](order-service-deep-dive/01-controller-and-api.md): Explains `OrderController`, DTO translation, `ResponseEntity`, and API design.
2. [Data & Persistence Layer](order-service-deep-dive/02-data-layer.md): Explains the `Order` entity, JPA, `@Enumerated`, and common database pitfalls.
3. [State Machine Configuration](order-service-deep-dive/03-state-machine-config.md): Explains how the rules of the Saga are defined and how state changes are intercepted.
4. [Orchestration & Reactive Programming](order-service-deep-dive/04-orchestration-and-reactive.md): Explains how `OrderService` interacts with the State Machine, including the shift to the Reactive (`Mono`) architecture in Spring 3.x.