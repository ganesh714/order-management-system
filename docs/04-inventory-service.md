# Inventory Service (The Worker)

## Role in the Architecture
The Inventory Service acts as a stateless mock worker service for this Saga implementation. It reacts to instructions and processes requests without keeping state locally in a database in this implementation.

## Main Responsibilities
- **Reserving Stock:** Deducting items from the available inventory capacity when receiving an orchestrator request (Forward process).

## API Endpoints
*   **POST `/api/inventory?orderId={id}`**: Reserves inventory for a specific order.

## Failure Simulation
Similar to the Payment Service, it includes a deterministic failure simulation to test compensating transactions. 
We use a Spring Property injected via `@Value("${fail.on.order.id}")`. If the incoming `orderId` matches this value, the controller intentionally returns a `500 Internal Server Error`. When this happens, it triggers a failure in the overall Saga, forcing the Orchestrator to execute the compensating transaction (the Payment Refund).

## Key Spring Classes & Annotations Used
* **`@RestController`**: Marks the class as a web controller.
* **`@RequestMapping("/api/inventory")`**: Sets the base URL path for all endpoints in the controller.
* **`@PostMapping`**: Maps incoming HTTP POST requests to the reservation method.
* **`@RequestParam`**: Extracts the `orderId` directly from the URL query string (e.g., `/api/inventory?orderId=201`).
* **`@Value("${fail.on.order.id}")`**: Injects the failure property directly from configuration.

