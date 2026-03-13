# Order Management System

A distributed order management system built with Spring Boot demonstrating the Saga Pattern using Spring State Machine.

## Documentation
Please check the `docs/` folder for in-depth details on the architecture and how the system works:
1. [Architecture and the Saga Pattern](docs/01-architecture-and-saga.md)
2. [Order Service (The Orchestrator)](docs/02-order-service.md)
3. [Payment Service (The Worker)](docs/03-payment-service.md)
4. [Inventory Service (The Worker)](docs/04-inventory-service.md)

## How to Run
This project is fully containerized using Docker and Docker Compose. This ensures you do not need to install PostgreSQL or configure Java environments manually.

### 1. Prerequisites
*   [Docker](https://docs.docker.com/get-docker/) installed and running.
*   [Docker Compose](https://docs.docker.com/compose/install/) installed.

### 2. Environment Setup
At the root of the project, copy the `.env.example` file to create a real `.env` file:
```bash
cp .env.example .env
```
*(If you are on Windows, simply copy/paste the file and rename it to `.env`)*.
Ensure the variables match your desired local testing setup.

### 3. Build and Start the System
Open a terminal at the root of the project and run:
```bash
docker-compose up --build
```
This will:
1.  Pull a PostgreSQL 14 image and start the `saga-db` database.
2.  Compile and build the `payment-service` and `inventory-service` images.
3.  Wait for the database to be healthy, then compile and start the `order-service` Orchestrator.

### 4. Testing the Saga Pattern
You can test the Orchestrator's behavior by sending HTTP POST requests to the `order-service` at `http://localhost:8080/orders`. 

We have provided a `submission.json` file in the root directory detailing the test scenarios.

#### Scenario A: The Happy Path (Complete Success)
Use an ID that will **not** trigger the simulated failures (e.g., `"100"` or any random UUID).
```json
{
    "customerId": 1,
    "productId": 1,
    "quantity": 2,
    "unitPrice": 50.00
}
```
*Result*: Order is created -> Payment is processed -> Inventory is reserved -> State becomes `ORDER_COMPLETED`.

#### Scenario B: Payment Failure
To test payment rejection, pass the ID defined in `submission.json` as `failingPaymentOrderId` (`201`) as your generic identifier. Because our controller takes generating ID internally, to test this specific failure with our current REST API, you'll need to update the Order Entity to accept a manual ID for testing, or trace the generated ID in the logs.
*Result*: Order is created -> Payment Fails -> State becomes `ORDER_FAILED`.

#### Scenario C: Inventory Failure (The Compensating Transaction!)
Wait for the Payment to succeed, but trigger a failure in the Inventory Service.
*Result*: Order is created -> Payment Succeeds -> Inventory Fails -> **PaymentService is called to Refund the money** -> State becomes `ORDER_FAILED`.
