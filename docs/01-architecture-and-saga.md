# Architecture and the Saga Pattern

## 1. The Core Problem (Microservices vs. Monoliths)
* **Monoliths**: Traditional databases handle transactions easily (everything succeeds or everything fails together).
* **Microservices**: These make distributed transactions hard. In our system, Order, Payment, and Inventory are separate apps/services. If one section fails, the others don't automatically know to undo or rollback their work.

## 2. The Saga Pattern (The Solution)
To solve the distributed transaction problem, we use the Saga pattern. 
* **Orchestration**: We use one central "Manager" (the **Order Service**) to tell the other services what to do. We chose Orchestration over Choreography to avoid having logic scattered across services, maintaining a single source of truth for the transaction flow.
* **Forward Transactions**: The happy path (Create Order -> Take Payment -> Reserve Inventory).
* **Compensating Transactions**: The "Undo" path. If the Inventory process fails, the manager explicitly tells the Payment service to refund the money so that system data stays consistent.

## 3. Spring State Machine (The Brain)
* **What it is**: A framework that manages complex flows without messy `if/else` statements.
* **States**: The status of the order (e.g., `ORDER_CREATED`, `PAYMENT_PENDING`).
* **Events**: The triggers that move the order from one state to another (e.g., `PAYMENT_SUCCESS`, `INVENTORY_FAILED`).
* **Persistence**: Saving the machine's current state to PostgreSQL so it remembers where it left off in case the server crashes.

## Flow Overview
* **Happy Path**: Create Order -> Process Payment -> Reserve Inventory -> Order Completed.
* **Failure Path**: Create Order -> Process Payment -> Reserve Inventory (Fails) -> Refund Payment -> Order Cancelled.
