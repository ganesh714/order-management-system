# Data & Persistence Layer Deep Dive

This layer handles how Java objects are mapped to the PostgreSQL database.

## Key Spring/JPA Classes Used
* **`@Entity` & `@Table`**: Tells Hibernate (Spring's database tool) to create a physical table named `orders` based on this Java class.
* **`JpaRepository`**: An interface that provides built-in methods like `.save()`, `.findById()`, and `.findAll()`, so we don't have to write raw SQL queries.

## Deep Dive: Enums in the Database
```java
@Enumerated(EnumType.STRING)
private OrderState status;
```

By default, if you save an Enum to a database, Java saves its ordinal integer (e.g., `ORDER_CREATED` = 0, `PAYMENT_PENDING` = 1). If a developer later adds a new Enum in the middle of the list, all the numbers shift, and your database gets corrupted. `@Enumerated(EnumType.STRING)` forces the database to save the actual text `"ORDER_CREATED"`, ensuring it is readable and future-proof.

## Deep Dive: getReferenceById vs findById

When fetching an order, you have two options in JPA:

1. `orderRepository.getReferenceById(id)`: This returns a **Lazy Proxy** (a fake, empty placeholder object). It doesn't hit the database until you explicitly call a getter (like `order.getAmount()`). If you pass this proxy back to the Controller and the database connection closes, it throws a `LazyInitializationException`.
2. `orderRepository.findById(id)`: This instantly hits the database and retrieves the full, populated object (Eager loading). We use this to ensure the data is fully loaded before the State Machine or Controller tries to use it.
