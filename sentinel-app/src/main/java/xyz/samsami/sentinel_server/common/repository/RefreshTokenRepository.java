package xyz.samsami.sentinel_server.common.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final ReactiveStringRedisTemplate redisTemplate;

    private static final String PREFIX = "refreshToken:";

    public Mono<Void> save(String email, String refreshToken, long expirationMillis) {
        return redisTemplate.opsForValue()
            .set(PREFIX + email, refreshToken, Duration.ofMillis(expirationMillis))
            .then();
    }

    public Mono<String> findByEmail(String email) {
        return redisTemplate.opsForValue().get(PREFIX + email);
    }

    public Mono<Void> deleteByEmail(String email) {
        return redisTemplate.delete(PREFIX + email).then();
    }
}
