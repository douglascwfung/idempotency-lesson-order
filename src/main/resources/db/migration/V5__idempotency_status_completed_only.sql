-- HEAD (post Lesson 12/13): only COMPLETED is a valid key status.
-- Teaching tags through lesson-13 remain historical and do not include this CHECK.
ALTER TABLE idempotency_keys
    ADD CONSTRAINT chk_idempotency_keys_status_completed
    CHECK (status = 'COMPLETED');
