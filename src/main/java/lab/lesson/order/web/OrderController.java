package lab.lesson.order.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab.lesson.idem.IdempotencyKey;
import lab.lesson.idem.IdempotencyService;
import lab.lesson.order.service.OrderService;
import lab.lesson.order.web.dto.CreateOrderRequest;
import lab.lesson.order.web.dto.OrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public OrderController(OrderService orderService,
                           IdempotencyService idempotencyService,
                           ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody CreateOrderRequest request) throws Exception {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return orderService.create(request);
        }

        if (userId == null || userId.isBlank()) {
            userId = "anonymous";
        }

        String rawBody = objectMapper.writeValueAsString(request);
        Optional<IdempotencyKey> replay = idempotencyService.claimOrReplay(idempotencyKey, userId, "/orders", rawBody);

        if (replay.isPresent()) {
            return objectMapper.readValue(replay.get().getResponseBody(), OrderResponse.class);
        }

        OrderResponse response = orderService.create(request);
        idempotencyService.complete(idempotencyKey, 201, objectMapper.writeValueAsString(response));
        return response;
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.get(id);
    }

    @PutMapping("/{id}")
    public OrderResponse replace(@PathVariable Long id, @RequestBody CreateOrderRequest request) {
        return orderService.replace(id, request);
    }
}
