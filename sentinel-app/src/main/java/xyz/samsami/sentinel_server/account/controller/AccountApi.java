package xyz.samsami.sentinel_server.account.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import xyz.samsami.sentinel_server.account.dto.AccountReqCreateDto;
import xyz.samsami.sentinel_server.account.dto.AccountReqLoginDto;
import xyz.samsami.sentinel_server.account.dto.AccountRespCreateDto;
import xyz.samsami.sentinel_server.common.dto.CommonRespDto;

@RequestMapping("/api")
@Tag(name = "Account API", description = "계정 관련 API")
public interface AccountApi {

    @Operation(summary = "계정 생성", description = "새로운 계정을 생성합니다.")
    @PostMapping("/accounts")
    Mono<CommonRespDto<AccountRespCreateDto>> createAccount(@RequestBody AccountReqCreateDto dto);

    @Operation(summary = "로그인", description = "계정 로그인 후 쿠키에 토큰을 저장합니다.")
    @PostMapping("/tokens:login")
    Mono<ResponseEntity<Void>> loginAccount(@RequestBody AccountReqLoginDto dto);

    @Operation(summary = "로그아웃", description = "계정 로그아웃 처리")
    @PostMapping("/tokens:logout")
    Mono<CommonRespDto<Void>> logoutAccount(ServerWebExchange exchange);
}