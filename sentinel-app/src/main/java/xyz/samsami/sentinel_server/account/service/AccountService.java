package xyz.samsami.sentinel_server.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import xyz.samsami.sentinel_server.account.dispatcher.AccountPostProcessorDispatcher;
import xyz.samsami.sentinel_server.account.domain.Account;
import xyz.samsami.sentinel_server.account.dto.AccountReqCreateDto;
import xyz.samsami.sentinel_server.account.dto.AccountReqLoginDto;
import xyz.samsami.sentinel_server.account.dto.AccountRespCreateDto;
import xyz.samsami.sentinel_server.account.dto.AccountRespLoginDto;
import xyz.samsami.sentinel_server.account.mapper.AccountMapper;
import xyz.samsami.sentinel_server.account.repository.AccountRepository;
import xyz.samsami.sentinel_server.common.exception.CommonException;
import xyz.samsami.sentinel_server.common.service.JwtService;
import xyz.samsami.sentinel_server.common.type.ExceptionType;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository repository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AccountPostProcessorDispatcher dispatcher;

    public Mono<AccountRespCreateDto> createAccount(AccountReqCreateDto dto) {
        return repository.findByEmail(dto.getEmail())
            .flatMap(account -> Mono.<Account>error(
                new CommonException(ExceptionType.BAD_REQUEST, "이미 가입된 이메일입니다."))
            )
            .switchIfEmpty(
                Mono.defer(() -> {
                    Account account = Account.builder()
                        .id(UUID.randomUUID())
                        .email(dto.getEmail())
                        .password(passwordEncoder.encode(dto.getPassword()))
                        .build();
                    return repository.save(account)
                        .flatMap(savedAccount -> dispatcher.process(dto.getType(), savedAccount, dto)
                        .thenReturn(savedAccount)
                    );
                })
            )
            .map(AccountMapper::toRespDto);
    }

    public Mono<AccountRespLoginDto> loginAccount(AccountReqLoginDto dto) {
        return repository.findByEmail(dto.getEmail())
            .switchIfEmpty(Mono.error(new CommonException(ExceptionType.NOT_FOUND, "가입된 이메일이 없습니다.")))
            .flatMap(account -> {
                if (!passwordEncoder.matches(dto.getPassword(), account.getPassword())) {
                    return Mono.error(new CommonException(ExceptionType.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."));
                }

                return jwtService.login(account.getEmail(), account.getId())
                    .map(tokenResponse -> new AccountRespLoginDto(
                        tokenResponse.getAccessToken(),
                        tokenResponse.getRefreshToken()
                    ));
            });
    }

    public Mono<Void> logoutAccount(ServerWebExchange exchange) {
        return jwtService.logout(exchange);
    }
}