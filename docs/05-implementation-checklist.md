# Implementation Checklist & Remaining Tasks

## Phase 1: Foundation (Completed)
- [x] Create project repository and `.gitignore`.
- [x] Add `.env.example` for environment variables.
- [x] Implement Order Service data layer (`Order`, `OrderState`, `OrderEvent`, `OrderRepository`).
- [x] Implement Order Service API layer (`OrderController`, `OrderRequest`, `OrderResponse`).

## Phase 2: State Machine Brain (Next Step)
- [ ] Create `StateMachineConfig.java`: Map out the strict rules (e.g., `ORDER_CREATED` + `PAYMENT_SUCCESS` = `PAYMENT_COMPLETED`).
- [ ] Create `StateMachineInterceptor.java`: Listen for state changes and update the `status` column in the PostgreSQL database.
- [ ] Connect `OrderService` to the State Machine so that creating an order sends the initial `CREATE_ORDER` event.

## Phase 3: The Integration Layer
- [ ] Create simple REST clients in the Order Service (using `RestTemplate` or `WebClient`) to send HTTP requests to the Payment and Inventory URLs.
- [ ] Create State Machine Actions: Tell the State Machine to use these REST clients when a state transitions (e.g., when entering `PAYMENT_PENDING`, call the Payment Service).

## Phase 4: The Worker Services
- [ ] Generate `payment-service` Spring Boot app. Create a simple controller that returns 200 OK, but returns 500 Error if `orderId` matches the fail configuration.
- [ ] Generate `inventory-service` Spring Boot app. Create a simple controller with similar pass/fail logic.

## Phase 5: Containerization & Finalization
- [ ] Write the `docker-compose.yml` to spin up PostgreSQL, `order-service`, `payment-service`, and `inventory-service` all together.
- [ ] Create `submission.json` containing the specific `orderId` values that trigger the failures.
- [ ] Perform End-to-End testing via Postman to verify the Happy Path and the Compensating (Rollback) Path.