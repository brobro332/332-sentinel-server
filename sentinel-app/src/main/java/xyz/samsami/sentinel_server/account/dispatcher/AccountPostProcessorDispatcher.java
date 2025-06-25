package xyz.samsami.sentinel_server.account.dispatcher;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import xyz.samsami.sentinel_server.account.domain.Account;
import xyz.samsami.sentinel_server.account.dto.AccountReqCreateDto;
import xyz.samsami.sentinel_server.account.processor.AccountPostProcessor;
import xyz.samsami.sentinel_server.account.type.AccountType;

import java.util.List;

@Component
public class AccountPostProcessorDispatcher {
    private final List<AccountPostProcessor> processors;

    public AccountPostProcessorDispatcher(List<AccountPostProcessor> processors) {
        this.processors = processors;
    }

    public Mono<Void> process(AccountType type, Account account, AccountReqCreateDto dto) {
        return processors.stream()
            .filter(p -> p.supports(type))
            .findFirst()
            .map(p -> p.postProcess(account, dto))
            .orElse(Mono.empty());
    }
}
