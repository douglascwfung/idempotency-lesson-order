package lab.lesson.idem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {

    Optional<IdempotencyKey> findFirstByRequestHashAndStatus(String requestHash, String status);
}
