# API & Controller Layer Deep Dive

The Controller acts as the "Front Desk" of the application. It receives HTTP requests, unpacks the data, hands it to the Service layer, and formats the HTTP response.

## Key Spring Classes Used
* **`@RestController`**: Tells Spring that this class handles web requests and automatically converts the returned Java objects into JSON.
* **`@PostMapping` / `@GetMapping`**: Maps specific HTTP methods and URLs (e.g., `/orders`) to Java methods.
* **`@RequestBody`**: Tells Spring to take the incoming JSON payload and automatically map it to our `OrderRequest` Java object.
* **`@PathVariable`**: Extracts values directly from the URL (e.g., the `{id}` in `/orders/{id}`).

## Deep Dive: ResponseEntity
```java
return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
```

`ResponseEntity` is a Spring class that represents the *entire* HTTP response. We use it instead of just returning the `OrderResponse` object because it gives us control over the **HTTP Status Code**.

* When creating an order, we return `202 ACCEPTED`. In an asynchronous Saga, returning 201 (Created) or 200 (OK) implies the whole process is done. 202 accurately tells the client: "We received your order, but payment and inventory are still processing."

## Design Principle: Controller vs. Service logic

In our `OrderController`, we write code to map data from the `OrderRequest` (DTO) into a new `Order` (Entity), and then map the saved `Order` back to an `OrderResponse` (DTO).

* **Why does this look heavy?** This is pure Data Mapping (Translation). The Controller's job is dealing with the web, so it translates Web Objects (DTOs) into Database Objects (Entities).
* **Thin Controller / Fat Service**: A core System Design principle is keeping business logic out of the controller. Notice that the Controller doesn't trigger the State Machine or validate inventory—it strictly translates data and passes it to the `OrderService`.
