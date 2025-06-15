package xyz.samsami.sentinel_server.account.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;
import xyz.samsami.sentinel_server.account.domain.Account;

import java.util.UUID;

public interface AccountRepository extends ReactiveMongoRepository<Account, UUID> {
    Mono<Account> findByEmail(String email);
}
