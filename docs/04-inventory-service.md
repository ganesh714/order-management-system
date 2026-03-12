# Inventory Service (The Worker)

## Role in the Architecture
The Inventory Service also acts as a stateless mock worker service for this Saga implementation. It reacts to instructions and processes requests without keeping state locally in a database.

## Main Responsibilities
- **Reserving Stock:** Deducting items from the available inventory capacity when receiving an orchestrator request (Forward process).

## Failure Simulation
Similar to the Payment Service, it includes a failure simulation logic. It intentionally fails when processing a specific `orderId`. When this happens, it triggers a failure in the overall Saga, forcing the compensating transactions to execute (e.g., instructing the Orchestrator to trigger the Payment Service to issue a refund).
