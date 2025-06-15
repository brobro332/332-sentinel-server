package xyz.samsami.sentinel_server.common.filter;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import xyz.samsami.sentinel_server.common.exception.CommonException;
import xyz.samsami.sentinel_server.common.provider.JwtProvider;
import xyz.samsami.sentinel_server.common.service.JwtService;
import xyz.samsami.sentinel_server.common.type.CookieType;
import xyz.samsami.sentinel_server.common.type.ExceptionType;

@Slf4j
@RequiredArgsConstructor
public class JwtWebFilter implements WebFilter {
    private final JwtService service;
    private final JwtProvider provider;

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String accessToken = provider.resolveToken(exchange, CookieType.ACCESS_TOKEN.getName());
        String refreshToken = provider.resolveToken(exchange, CookieType.REFRESH_TOKEN.getName());

        if (accessToken == null) return service.reissueAccessTokenAndProceed(exchange, chain, refreshToken);

        return service.isTokenBlacklisted(accessToken)
            .flatMap(isBlacklisted -> {
                if (isBlacklisted) return Mono.error(new CommonException(ExceptionType.FORBIDDEN, "권한이 없습니다."));

                if (provider.validateToken(accessToken)) {
                    Authentication authentication = provider.getAuthentication(accessToken);
                    return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                }

                return service.reissueAccessTokenAndProceed(exchange, chain, refreshToken);
            });
    }
}