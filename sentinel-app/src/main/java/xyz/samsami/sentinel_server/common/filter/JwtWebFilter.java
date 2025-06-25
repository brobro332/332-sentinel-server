package xyz.samsami.sentinel_server.common.filter;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import xyz.samsami.sentinel_server.common.exception.CommonException;
import xyz.samsami.sentinel_server.common.provider.JwtProvider;
import xyz.samsami.sentinel_server.common.service.JwtService;
import xyz.samsami.sentinel_server.common.type.CookieType;
import xyz.samsami.sentinel_server.common.type.ExceptionType;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtWebFilter implements WebFilter {
    private final JwtService service;
    private final JwtProvider provider;
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> EXCLUDE_PATHS = List.of(
        "/api/accounts/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/webjars/**"
    );

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        boolean excluded = EXCLUDE_PATHS.stream()
            .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (excluded) return chain.filter(exchange);

        String accessToken = provider.resolveToken(exchange, CookieType.ACCESS_TOKEN.getName());
        String refreshToken = provider.resolveToken(exchange, CookieType.REFRESH_TOKEN.getName());

        if (accessToken == null) {
            if (refreshToken == null) return Mono.error(new CommonException(ExceptionType.UNAUTHORIZED, "로그인이 필요합니다."));
            return service.reissueAccessTokenAndProceed(exchange, chain, refreshToken);
        }

        if (!provider.validateToken(accessToken)) {
            return service.reissueAccessTokenAndProceed(exchange, chain, refreshToken);
        }

        return service.isTokenBlacklisted(accessToken)
            .flatMap(isBlacklisted -> {
                if (isBlacklisted) return Mono.error(new CommonException(ExceptionType.FORBIDDEN, "권한이 없습니다."));

                Authentication authentication = provider.getAuthentication(accessToken);
                return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            });
    }
}