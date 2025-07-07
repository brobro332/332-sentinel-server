package xyz.samsami.sentinel_server.common.filter;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
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
        ServerHttpRequest request = exchange.getRequest();
        if (isOptionsRequest(request)) return chain.filter(exchange);
        if (isExcludedPath(request)) return chain.filter(exchange);

        String accessToken = resolveAccessToken(exchange);
        String refreshToken = resolveRefreshToken(exchange);

        if (accessToken == null) return handleNoAccessToken(refreshToken, exchange, chain);
        if (!isValidToken(accessToken)) return handleTokenReissue(refreshToken, exchange, chain);

        return checkBlacklistAndAuthenticate(accessToken, exchange, chain);
    }

    private boolean isOptionsRequest(ServerHttpRequest request) {
        return request.getMethod() == HttpMethod.OPTIONS;
    }

    private boolean isExcludedPath(ServerHttpRequest request) {
        String path = request.getPath().value();
        boolean excluded = EXCLUDE_PATHS.stream()
            .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (request.getMethod() == HttpMethod.GET && "/api/accounts/session".equals(path)) {
            excluded = false;
        }

        return excluded;
    }

    private String resolveAccessToken(ServerWebExchange exchange) {
        return provider.resolveToken(exchange, CookieType.ACCESS_TOKEN.getName());
    }

    private String resolveRefreshToken(ServerWebExchange exchange) {
        return provider.resolveToken(exchange, CookieType.REFRESH_TOKEN.getName());
    }

    private Mono<Void> handleNoAccessToken(String refreshToken, ServerWebExchange exchange, WebFilterChain chain) {
        if (refreshToken == null) {
            return Mono.error(new CommonException(ExceptionType.UNAUTHORIZED, "로그인이 필요합니다."));
        }

        return service.reissueAccessTokenAndProceed(exchange, chain, refreshToken);
    }

    private boolean isValidToken(String token) {
        return provider.validateToken(token);
    }

    private Mono<Void> handleTokenReissue(String refreshToken, ServerWebExchange exchange, WebFilterChain chain) {
        return service.reissueAccessTokenAndProceed(exchange, chain, refreshToken);
    }

    private Mono<Void> checkBlacklistAndAuthenticate(String accessToken, ServerWebExchange exchange, WebFilterChain chain) {
        return service.isTokenBlacklisted(accessToken)
            .flatMap(isBlacklisted -> {
                if (isBlacklisted) {
                    return Mono.error(new CommonException(ExceptionType.FORBIDDEN, "권한이 없습니다."));
                }
                Authentication authentication = provider.getAuthentication(accessToken);
                return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            });
    }
}