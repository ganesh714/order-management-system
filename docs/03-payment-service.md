# Payment Service (The Worker)

## Role in the Architecture
The Payment Service operates as a stateless mock worker service for this Saga implementation. It does not control the saga state and only listens to instructions. It does not maintain its own database in this implementation.

## Main Responsibilities
It has two major duties in the Saga flow based on orchestration commands from the Order Service:
1. **Processing Payments:** Deducting funds when a standard payment request is received (Forward process).
2. **Refunding/Canceling Payments:** Refunding the payment if a compensating transaction is triggered by the Saga Orchestrator (Undo process).

## API Endpoints
*   **POST `/api/payments?orderId={id}`**: Processes a payment for a specific order.
*   **DELETE `/api/payments/{id}/cancel`**: Refunds a payment for a specific order.

## Failure Simulation
To demonstrate saga failure handling and compensating transactions, the Payment Service includes a deterministic failure simulation.
We use a Spring Property (`fail.on.order.id`) defined in `application.properties` (or an environment variable `FAIL_ORDER_ID`). If the incoming `orderId` matches this value (default is `201`), the controller intentionally returns a `500 Internal Server Error`. This artificially triggers a failure event back to the Orchestrator.

## Key Spring Classes & Annotations Used
* **`@RestController`**: Marks the class as a web controller.
* **`@RequestMapping("/api/payments")`**: Sets the base URL path for all endpoints in the controller.
* **`@PostMapping` / `@DeleteMapping`**: Maps HTTP POST and DELETE requests to specific handler methods.
* **`@RequestParam`**: Extracts the `orderId` from the URL query string (used in the POST endpoint).
* **`@PathVariable`**: Extracts the `orderId` from the URL path (used in the DELETE/Refund endpoint).
* **`@Value("${fail.on.order.id}")`**: Injects the failure order ID from configuration.

