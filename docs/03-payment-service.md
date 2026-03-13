# Payment Service (The Worker)

## Role in the Architecture
The Payment Service operates as a stateless mock worker service for this Saga implementation. It does not control the saga state and only listens to instructions. It also does not maintain its own database.

## Main Responsibilities
It has two major duties in the Saga flow based on orchestration commands from the Order Service:
1. **Processing Payments:** Deducting funds when a standard payment request is received (Forward process).
2. **Refunding/Canceling Payments:** Refunding the payment if a compensating transaction is triggered by the Saga Orchestrator (Undo process).

## Failure Simulation
To demonstrate saga failure handling and compensating transactions, the Payment Service includes a deterministic failure simulation.
Instead of an external config file, we use a Spring Property (`fail.on.order.id`) defined in `application.properties` (or an environment variable `FAIL_PAYMENT_ORDER_ID`). If the incoming `orderId` matches this value (default is `201`), the controller intentionally returns a `500 Internal Server Error`. This artificially triggers a failure event back to the Orchestrator, allowing us to safely test the state machine's failure and recovery loops.

## Key Spring Classes & Annotations Used
* **`@RestController`**: Marks the class as a web controller where every method returns a domain object or a `ResponseEntity`, automatically serializing responses to JSON or plain text.
* **`@PostMapping` / `@DeleteMapping`**: Maps HTTP POST and DELETE requests to specific handler methods. POST is used to process a payment (Forward transaction), while DELETE is used semantically to cancel or refund a payment (Compensating transaction).
* **`@RequestBody`**: Extracts the main HTTP request body and maps it directly to a Java object/String (used in the POST to get the `orderId`).
* **`@RequestParam`**: Extracts values directly from the URL query string (e.g., `/payments?orderId=123`). Used in the DELETE endpoint to know exactly which payment to refund.
* **`@Value("${KEY:default}")`**: This is a powerful Spring annotation that injects values straight from `application.properties` or System Environment Variables directly into Java variables. In this service, `@Value("${FAIL_PAYMENT_ORDER_ID:201}")` tells Spring: "Look for an environment variable named `FAIL_PAYMENT_ORDER_ID`. If you can't find it, safely fall back to the string `"201"`". This allows us to modify failure tests dynamically via Docker without ever touching the Java code!
