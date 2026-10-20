package lab.lesson.idem;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class IdempotencyService {

    private final IdempotencyKeyRepository repository;

    public IdempotencyService(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    /**
     * Inserts the key already as COMPLETED, runs the Order write, and stores the response
     * in one transaction. There is no IN_PROGRESS status. A crash rolls the key and the
     * Order back together. A concurrent insert of the same key hits the primary key; the
     * service reloads and either replays the completed response or rejects a hash mismatch.
     */
    @Transactional
    public <T> T runOrReplay(String key,
                             String userId,
                             String path,
                             String rawBody,
                             int responseStatus,
                             ThrowingSupplier<T> work,
                             ThrowingFunction<T, String> serializer,
                             ThrowingFunction<String, T> deserializer) throws Exception {
        String hash = hash(userId, path, rawBody);
        Optional<IdempotencyKey> existing = repository.findById(key);

        if (existing.isPresent()) {
            return replayOrReject(existing.get(), key, hash, deserializer);
        }

        IdempotencyKey created = new IdempotencyKey();
        created.setKeyValue(key);
        created.setRequestHash(hash);
        created.setStatus("COMPLETED");

        try {
            repository.saveAndFlush(created);
        } catch (DataIntegrityViolationException e) {
            IdempotencyKey raced = repository.findById(key).orElseThrow();
            return replayOrReject(raced, key, hash, deserializer);
        }

        T result = work.get();
        created.setResponseStatus(responseStatus);
        created.setResponseBody(serializer.apply(result));
        repository.save(created);
        return result;
    }

    private <T> T replayOrReject(IdempotencyKey found,
                                 String key,
                                 String hash,
                                 ThrowingFunction<String, T> deserializer) throws Exception {
        if (!found.getRequestHash().equals(hash)) {
            throw new IdempotencyKeyReuseException(key);
        }
        return deserializer.apply(found.getResponseBody());
    }

    private String hash(String userId, String path, String rawBody) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = userId + path + rawBody;
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R> {
        R apply(T value) throws Exception;
    }
}
