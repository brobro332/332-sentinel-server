package xyz.samsami.sentinel_server.common.util;

import org.springframework.http.ResponseCookie;
import xyz.samsami.sentinel_server.common.type.CookieType;

import java.time.Duration;

public class CookieUtil {
    private static final boolean HTTP_ONLY = true;
    private static final boolean SECURE = true;
    private static final String PATH = "/";
    private static final Duration ACCESS_TOKEN_MAX_AGE = Duration.ofHours(1);
    private static final Duration REFRESH_TOKEN_MAX_AGE = Duration.ofDays(7);

    public static ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from(CookieType.ACCESS_TOKEN.getName(), token)
            .httpOnly(HTTP_ONLY)
            .secure(SECURE)
            .path(PATH)
            .maxAge(ACCESS_TOKEN_MAX_AGE)
            .build();
    }

    public static ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from(CookieType.REFRESH_TOKEN.getName(), token)
            .httpOnly(HTTP_ONLY)
            .secure(SECURE)
            .path(PATH)
            .maxAge(REFRESH_TOKEN_MAX_AGE)
            .build();
    }
}