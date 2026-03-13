# Inventory Service (The Worker)

## Role in the Architecture
The Inventory Service also acts as a stateless mock worker service for this Saga implementation. It reacts to instructions and processes requests without keeping state locally in a database.

## Main Responsibilities
- **Reserving Stock:** Deducting items from the available inventory capacity when receiving an orchestrator request (Forward process).

## Failure Simulation
Similar to the Payment Service, it includes a deterministic failure simulation to test compensating transactions. 
Instead of an external config file or hard-coded logic, we use a Spring Property injected via `@Value("${fail.on.order.id}")`. If the incoming `orderId` matches this value, the controller intentionally returns a `500 Internal Server Error`. When this happens, it triggers a failure in the overall Saga, forcing the Orchestrator to execute the compensating transaction (the Payment Refund).

## Key Spring Classes & Annotations Used
* **`@RestController`**: Marks the class as a web controller, allowing it to naturally return `ResponseEntity` objects that are serialized to pure text or JSON.
* **`@PostMapping("/inventory")`**: Maps incoming HTTP POST requests (which are triggered by the `ReserveInventoryAction` in the Orchestrator) to the reservation method.
* **`@RequestParam`**: Unlike the Payment Service which used `@RequestBody` for POSTs, the `InventoryController` extracts the `orderId` directly from the URL query string (e.g., `/inventory?orderId=201`).
* **`@Value("${fail.on.order.id}")`**: Injects the failure property directly from `application.properties`. This allows us to share the exact same configuration key name across both the Payment and Inventory services if we choose, or define them individually in Docker context.
