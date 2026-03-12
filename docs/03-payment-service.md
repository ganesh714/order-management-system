# Payment Service (The Worker)

## Role in the Architecture
The Payment Service operates as a stateless mock worker service for this Saga implementation. It does not control the saga state and only listens to instructions. It also does not maintain its own database.

## Main Responsibilities
It has two major duties in the Saga flow based on orchestration commands from the Order Service:
1. **Processing Payments:** Deducting funds when a standard payment request is received (Forward process).
2. **Refunding/Canceling Payments:** Refunding the payment if a compensating transaction is triggered by the Saga Orchestrator (Undo process).

## Failure Simulation
To demonstrate saga failure handling and compensate transactions, the Payment Service includes a failure simulation.
It can read a config file (`submission.json` or similar) to intentionally force a `500 Internal Server Error` when it processes a specific `orderId`. This artificially triggers a failure event back to the Orchestrator, allowing testing of the state machine.
