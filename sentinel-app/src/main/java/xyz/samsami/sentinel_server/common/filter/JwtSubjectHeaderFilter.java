package xyz.samsami.sentinel_server.common.filter;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtSubjectHeaderFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String accessToken = null;

        HttpCookie cookie = exchange.getRequest().getCookies().getFirst("access_token");
        if (cookie != null) accessToken = cookie.getValue();

        if (accessToken != null) {
            try {
                JWTClaimsSet claims = JWTParser.parse(accessToken).getJWTClaimsSet();
                String subject = claims.getSubject();

                ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(r ->
                        r.headers(headers ->
                            headers.add("X-Account-Id", subject)))
                    .build();

                return chain.filter(mutatedExchange);
            } catch (Exception e) {
                /* 아무 처리하지 않음 */
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}