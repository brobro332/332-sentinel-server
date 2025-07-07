package xyz.samsami.sentinel_server.account.processor;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import xyz.samsami.sentinel_server.account.domain.Account;
import xyz.samsami.sentinel_server.account.dto.AccountReqCreateDto;
import xyz.samsami.sentinel_server.account.dto.BlokeyLandReqDto;
import xyz.samsami.sentinel_server.account.type.AccountType;

@Component
public class BlokeyLandPostProcessor implements AccountPostProcessor {
    private final WebClient webClient;

    public BlokeyLandPostProcessor(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://spring-boot-app:8081").build();
    }

    @Override
    public boolean supports(AccountType type) {
        return type == AccountType.BLOKEY_LAND;
    }

    @Override
    public Mono<Void> postProcess(Account account, AccountReqCreateDto dto) {
        BlokeyLandReqDto blokeyDto = BlokeyLandReqDto.builder()
            .id(account.getId())
            .nickname(dto.getNickname())
            .bio(dto.getBio())
            .build();

        return webClient.post()
            .uri("/api/blokeys")
            .bodyValue(blokeyDto)
            .retrieve()
            .bodyToMono(Void.class);
    }
}
