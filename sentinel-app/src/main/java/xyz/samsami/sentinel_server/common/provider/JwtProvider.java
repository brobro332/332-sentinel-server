package xyz.samsami.sentinel_server.common.provider;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import xyz.samsami.sentinel_server.common.exception.CommonException;
import xyz.samsami.sentinel_server.common.type.ExceptionType;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    @Value("${jwt.secret-key}")
    private String secretKey;

    private static final long ACCESS_TOKEN_EXPIRATION_MILLIS = 60 * 60 * 1_000;
    private static final long REFRESH_TOKEN_EXPIRATION_MILLIS = 7 * 24 * 60 * 60 * 1_000;

    public String createAccessToken(String email) throws JOSEException {
        JWSSigner signer = new MACSigner(secretKey.getBytes(StandardCharsets.UTF_8));

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject(email)
            .issueTime(new Date())
            .expirationTime(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_MILLIS))
            .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public String createRefreshToken(String email) throws JOSEException {
        JWSSigner signer = new MACSigner(secretKey.getBytes(StandardCharsets.UTF_8));

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject(email)
            .issueTime(new Date())
            .expirationTime(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_MILLIS))
            .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public Authentication getAuthentication(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secretKey.getBytes(StandardCharsets.UTF_8));

            if (!signedJWT.verify(verifier)) throw new CommonException(ExceptionType.UNAUTHORIZED, null);

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            String username = claims.getSubject();

            return new UsernamePasswordAuthenticationToken(username, null, null);
        } catch (Exception e) {
            throw new CommonException(ExceptionType.UNAUTHORIZED, null);
        }
    }

    public String resolveToken(ServerWebExchange exchange, String cookieName) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(cookieName);
        if (cookie == null) return null;

        return cookie.getValue();
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secretKey.getBytes(StandardCharsets.UTF_8));

            if (!signedJWT.verify(verifier)) return false;

            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expirationTime != null && !expirationTime.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String extractEmail(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            throw new CommonException(ExceptionType.INTERNAL_SERVER_ERROR, null);
        }
    }

    public Date extractExpirationTime(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getExpirationTime();
        } catch (ParseException e) {
            throw new CommonException(ExceptionType.INTERNAL_SERVER_ERROR, null);
        }
    }
}