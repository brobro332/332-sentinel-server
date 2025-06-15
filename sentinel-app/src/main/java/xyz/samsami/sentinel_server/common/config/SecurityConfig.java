package xyz.samsami.sentinel_server.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import xyz.samsami.sentinel_server.common.filter.JwtWebFilter;
import xyz.samsami.sentinel_server.common.provider.JwtProvider;
import xyz.samsami.sentinel_server.common.service.JwtService;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtService service;
    private final JwtProvider provider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .authorizeExchange(exchange -> exchange
                .pathMatchers(
                    "/api/accounts", "/api/tokens:*",
                    "/blokey-land/api/blokeys",
                    "/swagger-ui/**", "/v3/api-docs/**"
                ).permitAll()
                .anyExchange().authenticated()
            ).addFilterAt(new JwtWebFilter(service, provider), SecurityWebFiltersOrder.AUTHENTICATION);
        return http.build();
    }
}