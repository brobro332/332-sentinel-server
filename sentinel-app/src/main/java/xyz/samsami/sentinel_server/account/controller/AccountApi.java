package xyz.samsami.sentinel_server.account.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import xyz.samsami.sentinel_server.account.dto.AccountReqCreateDto;
import xyz.samsami.sentinel_server.account.dto.AccountReqLoginDto;
import xyz.samsami.sentinel_server.account.dto.AccountRespCreateDto;
import xyz.samsami.sentinel_server.common.dto.CommonRespDto;

@RequestMapping("/api/accounts")
@Tag(name = "Account API", description = "계정 관련 API")
public interface AccountApi {

    @Operation(summary = "계정 생성", description = "새로운 계정을 생성합니다.")
    @PostMapping
    Mono<CommonRespDto<AccountRespCreateDto>> createAccount(@RequestBody AccountReqCreateDto dto);

    @Operation(summary = "로그인", description = "계정 로그인 후 쿠키에 토큰을 저장합니다.")
    @PostMapping("/session")
    Mono<ResponseEntity<Void>> loginAccount(@RequestBody AccountReqLoginDto dto);

    @Operation(summary = "로그아웃", description = "계정 로그아웃 처리")
    @DeleteMapping("/session")
    Mono<CommonRespDto<Void>> logoutAccount(ServerWebExchange exchange);

    @Operation(summary = "로그인 상태 확인", description = "유효한 토큰인지 확인합니다.")
    @GetMapping("/session")
    default Mono<ResponseEntity<Void>> validateAccount(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok().build());
    }
}