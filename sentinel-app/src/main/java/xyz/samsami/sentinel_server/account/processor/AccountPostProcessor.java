package xyz.samsami.sentinel_server.account.processor;

import reactor.core.publisher.Mono;
import xyz.samsami.sentinel_server.account.domain.Account;
import xyz.samsami.sentinel_server.account.dto.AccountReqCreateDto;
import xyz.samsami.sentinel_server.account.type.AccountType;

public interface AccountPostProcessor {
    boolean supports(AccountType type);
    Mono<Void> postProcess(Account account, AccountReqCreateDto dto);
}