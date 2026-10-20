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

## Lesson Map (reading order)

| Lesson | Focus |
|--------|--------|
| 1 | Order aggregate + REST create/get/replace (duplicates allowed) |
| 2 | `@Transactional` service boundary |
| 3 | Optimistic locking with `@Version` on Order |
| 9 | Line `@Version` and single-line endpoints |
| 4 | Idempotency key claim and replay on create |
| 5 | User id in the hash; key-reuse rejection |
| 6 | `X-User-Id` header; 409 body with running key |
| 7 | Authenticated principal as user id |
| 8 | Primary-key race on concurrent claim |
| 10 | Same-content in-progress check |
| 11 | Required idempotency key on add-line |
| 12 | Single transaction: key + Order as `COMPLETED` |
| 13 | Lambda work callback into `runOrReplay` |

Lesson numbers stay fixed. **Reading order** places Lesson 9 (line version / single-line endpoints) right after Lesson 3 so optimistic locking on the Order root leads into the same idea on Order lines before the idempotency-key arc. The git tag `lesson-9` is historical: tag order follows when each lesson landed, which is not the same as this ideal reading order.

Full narrative: [`docs/lesson-plan.md`](docs/lesson-plan.md).

Design notes:

- [`docs/claim-vs-single-transaction.md`](docs/claim-vs-single-transaction.md) — claim-then-complete vs single-transaction (Lesson 12)
- [`docs/why-lambda-for-idempotency.md`](docs/why-lambda-for-idempotency.md) — why domain work is a lambda (Lesson 13)

## Tags and Historical Verify

Each lesson ends at a git tag named `lesson-N`. Tags include **`lesson-0`** (empty starting point) through **`lesson-13`**, including **`lesson-11`**.

```bash
git checkout lesson-N          # inspect code as of that lesson
git diff lesson-N lesson-N+1   # see what a lesson introduced
```

**Historical Verify:** the Verify sections in `docs/lesson-plan.md` on `main` may name HEAD test methods (for example `*RunOrReplay*`). To run the Verify commands against the code and tests as written for that lesson, check out the matching tag first (`git checkout lesson-N`), then run the `mvn -Dtest=...` commands from that lesson's section. Behaviour that landed on `main` after a tag (for example making add-line's `Idempotency-Key` required) is not rewritten into earlier tags; look for a tip tag on `main` when noted in the commit history.
