package xyz.samsami.sentinel_server.common.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class BlacklistRepository {

    private final ReactiveStringRedisTemplate redisTemplate;

    private static final String PREFIX = "blacklist:";

    public Mono<Void> save(String token, long expirationMillis) {
        return redisTemplate.opsForValue()
            .set(PREFIX + token, "blacklisted", Duration.ofMillis(expirationMillis))
            .then();
    }

    public Mono<Boolean> exists(String token) {
        return redisTemplate.hasKey(PREFIX + token);
    }
}