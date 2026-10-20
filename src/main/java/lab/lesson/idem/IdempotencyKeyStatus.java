package lab.lesson.idem;

/**
 * Allowed values for {@link IdempotencyKey#getStatus()} on the final HEAD design.
 *
 * <p>Lesson 12 removed {@code IN_PROGRESS}. Keys are inserted already as
 * {@link #COMPLETED} inside the same transaction as the Order write. The database
 * enforces the same rule with a CHECK constraint (Flyway {@code V5}).
 */
public enum IdempotencyKeyStatus {
    COMPLETED
}
