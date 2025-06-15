package xyz.samsami.sentinel_server.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import xyz.samsami.sentinel_server.common.type.ExceptionType;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Slf4j
@Order(-2)
@Component
@RequiredArgsConstructor
public class CommonExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @NonNull
    @Override
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        if (ex instanceof CommonException e) {
            return handleCommonException(exchange, e);
        } else if (ex instanceof JOSEException) {
            return handleJwtException(exchange, ex);
        } else {
            return handleUnknownException(exchange, ex);
        }
    }

    private Mono<Void> handleCommonException(ServerWebExchange exchange, CommonException e) {
        log.error("🔺 공통 예외 발생", e);
        ExceptionType type = e.getException();
        CommonExceptionEntity response = CommonExceptionEntity.builder()
            .code(type.getCode())
            .message(e.getMessage() != null ? e.getMessage() : type.getMessage())
            .timestamp(OffsetDateTime.now(ZoneId.of("Asia/Seoul")))
            .build();
        exchange.getResponse().setStatusCode(HttpStatus.valueOf(type.getStatus()));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return writeResponse(exchange, response);
    }

    private Mono<Void> handleJwtException(ServerWebExchange exchange, Throwable ex) {
        log.error("🔺 JWT 관련 예외 발생", ex);
        CommonExceptionEntity response = CommonExceptionEntity.builder()
            .code(ExceptionType.UNAUTHORIZED.getCode())
            .message(ExceptionType.UNAUTHORIZED.getMessage())
            .timestamp(OffsetDateTime.now(ZoneId.of("Asia/Seoul")))
            .build();
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return writeResponse(exchange, response);
    }

    private Mono<Void> handleUnknownException(ServerWebExchange exchange, Throwable ex) {
        log.error("🔺 예상치 못한 예외 발생", ex);
        CommonExceptionEntity response = CommonExceptionEntity.builder()
            .code(ExceptionType.INTERNAL_SERVER_ERROR.getCode())
            .message(ExceptionType.INTERNAL_SERVER_ERROR.getMessage())
            .timestamp(OffsetDateTime.now(ZoneId.of("Asia/Seoul")))
            .build();
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return writeResponse(exchange, response);
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange, CommonExceptionEntity response) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(response);
            DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
            return exchange.getResponse().writeWith(Mono.just(bufferFactory.wrap(bytes)));
        } catch (Exception writeEx) {
            log.error("🔺 응답 변환 중 오류", writeEx);
            return Mono.error(writeEx);
        }
    }
}