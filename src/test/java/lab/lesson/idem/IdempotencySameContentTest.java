package lab.lesson.idem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencySameContentTest {

    @Mock
    private IdempotencyKeyRepository repository;

    @InjectMocks
    private IdempotencyService service;

    @Test
    void newKeyWithSameContentInProgressReturnsRunningKey() {
        when(repository.findById("key-new")).thenReturn(Optional.empty());

        IdempotencyKey running = new IdempotencyKey();
        running.setKeyValue("key-running");
        running.setRequestHash(serviceHash());
        running.setStatus("IN_PROGRESS");
        when(repository.findFirstByRequestHashAndStatus(serviceHash(), "IN_PROGRESS"))
                .thenReturn(Optional.of(running));

        assertThatThrownBy(() -> service.claimOrReplay("key-new", "user-1", "/orders", "{}"))
                .isInstanceOf(IdempotencyInProgressException.class)
                .hasFieldOrPropertyWithValue("key", "key-running");
    }

    private String serviceHash() {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest("user-1/orders{}".getBytes());
            return java.util.HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
