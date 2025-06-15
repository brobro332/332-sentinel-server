package xyz.samsami.sentinel_server.common.service;

import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import xyz.samsami.sentinel_server.account.dto.AccountRespLoginDto;
import xyz.samsami.sentinel_server.common.exception.CommonException;
import xyz.samsami.sentinel_server.common.provider.JwtProvider;
import xyz.samsami.sentinel_server.common.repository.BlacklistRepository;
import xyz.samsami.sentinel_server.common.repository.RefreshTokenRepository;
import xyz.samsami.sentinel_server.common.type.CookieType;
import xyz.samsami.sentinel_server.common.type.ExceptionType;
import xyz.samsami.sentinel_server.common.util.CookieUtil;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProvider provider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistRepository blacklistRepository;

    private static final long REFRESH_TOKEN_EXPIRATION_MILLIS = 7 * 24 * 60 * 60 * 1_000;

    public Mono<AccountRespLoginDto> login(String email) {
        try {
            String accessToken = provider.createAccessToken(email);
            String refreshToken = provider.createRefreshToken(email);

            return saveRefreshToken(email, refreshToken)
                .thenReturn(new AccountRespLoginDto(accessToken, refreshToken));
        } catch (Exception e) {
            return Mono.error(new CommonException(ExceptionType.INTERNAL_SERVER_ERROR, "토큰 발급 실패"));
        }
    }

    public Mono<Void> logout(ServerWebExchange exchange) {
        String accessToken = provider.resolveToken(exchange, CookieType.ACCESS_TOKEN.getName());
        if (accessToken == null) return Mono.error(new CommonException(ExceptionType.UNAUTHORIZED, null));

        String email = provider.extractEmail(accessToken);
        Date expirationTime = provider.extractExpirationTime(accessToken);

        return Mono.when(
            deleteRefreshToken(email),
            saveBlacklist(accessToken, expirationTime)
        );
    }

    public Mono<Void> reissueAccessTokenAndProceed(ServerWebExchange exchange, WebFilterChain chain, String refreshToken) {
        if (refreshToken == null || !provider.validateToken(refreshToken)) return chain.filter(exchange);

        String email = provider.extractEmail(refreshToken);

        return isRefreshTokenExists(email, refreshToken)
            .flatMap(exists -> {
                if (!exists) return Mono.error(new CommonException(ExceptionType.UNAUTHORIZED, null));

                String newAccessToken;
                try {
                    newAccessToken = provider.createAccessToken(email);
                } catch (JOSEException e) {
                    return Mono.error(new CommonException(ExceptionType.INTERNAL_SERVER_ERROR, "토큰 생성 실패"));
                }

                ResponseCookie accessTokenCookie = CookieUtil.createAccessTokenCookie(newAccessToken);
                exchange.getResponse().addCookie(accessTokenCookie);

                Authentication authentication = provider.getAuthentication(newAccessToken);
                return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            });
    }

    public Mono<Void> saveRefreshToken(String email, String refreshToken) {
        return refreshTokenRepository.save(email, refreshToken, REFRESH_TOKEN_EXPIRATION_MILLIS);
    }

    public Mono<Boolean> isRefreshTokenExists(String email, String refreshToken) {
        return refreshTokenRepository.findByEmail(email)
            .map(storedToken -> storedToken.equals(refreshToken))
            .defaultIfEmpty(false);
    }

    public Mono<Void> deleteRefreshToken(String email) {
        return refreshTokenRepository.deleteByEmail(email);
    }

    public Mono<Void> saveBlacklist(String accessToken, Date expirationTime) {
        long ttlMillis = expirationTime.getTime() - System.currentTimeMillis();

        if (ttlMillis <= 0) return Mono.empty();

        return blacklistRepository.save(accessToken, ttlMillis);
    }

    public Mono<Boolean> isTokenBlacklisted(String token) {
        return blacklistRepository.exists(token);
    }
}
