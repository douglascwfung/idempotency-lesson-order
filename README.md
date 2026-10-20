# Idempotency Lesson: Order + Order Lines

This repository teaches progressive Spring Boot concepts using an Order aggregate that owns Order Lines.

The sequence covers parent-child aggregates, `@Transactional` boundaries, optimistic locking, and idempotency keys.

## How to Run

Requirements: Java 21 and Maven.

```
mvn spring-boot:run
```

The application starts on port 8080 and uses an in-memory H2 database.

## Example Interactions (Lesson 1)

Create an order:

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Alice",
    "lines": [
      { "sku": "SKU-1", "quantity": 2, "unitPrice": 10.00 },
      { "sku": "SKU-2", "quantity": 1, "unitPrice": 25.50 }
    ]
  }'
```

Retrieve the order (replace `1` with the id returned by create):

```bash
curl http://localhost:8080/orders/1
```

Replace the order content:

```bash
curl -X PUT http://localhost:8080/orders/1 \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Alice Updated",
    "lines": [
      { "sku": "SKU-3", "quantity": 5, "unitPrice": 7.99 }
    ]
  }'
```

Send the same create request twice. Lesson 1 deliberately creates two separate Orders.

## Lesson Plan

Follow the lessons in `docs/lesson-plan.md`.

Each lesson ends at a git tag named `lesson-N`. Use `git diff lesson-N lesson-N+1` to see the changes introduced by a lesson.

Lesson 0 is the empty starting point. Lesson 1 introduces the Order aggregate and REST endpoints that deliberately allow duplicate Orders on repeated creates.
