package xyz.samsami.sentinel_server.account.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import xyz.samsami.sentinel_server.account.dto.AccountReqCreateDto;
import xyz.samsami.sentinel_server.account.dto.AccountReqLoginDto;
import xyz.samsami.sentinel_server.account.dto.AccountRespCreateDto;
import xyz.samsami.sentinel_server.account.service.AccountService;
import xyz.samsami.sentinel_server.common.dto.CommonRespDto;
import xyz.samsami.sentinel_server.common.type.ResultType;
import xyz.samsami.sentinel_server.common.util.CookieUtil;

@RestController
@RequiredArgsConstructor
public class AccountController implements AccountApi {
    private final AccountService service;

    @Override
    public Mono<CommonRespDto<AccountRespCreateDto>> createAccount(@RequestBody AccountReqCreateDto dto) {
        return service.createAccount(dto).map(response ->
            CommonRespDto.of(ResultType.SUCCESS, "계정 등록 완료", response)
        );
    }

    @Override
    public Mono<ResponseEntity<Void>> loginAccount(@RequestBody AccountReqLoginDto dto) {
        return service.loginAccount(dto)
            .map(response -> {
                ResponseCookie accessTokenCookie = CookieUtil.createAccessTokenCookie(response.getAccessToken());
                ResponseCookie refreshTokenCookie = CookieUtil.createRefreshTokenCookie(response.getRefreshToken());

                return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .build();
            });
    }

    @Override
    public Mono<CommonRespDto<Void>> logoutAccount(ServerWebExchange exchange) {
        return service.logoutAccount(exchange).map(response ->
            CommonRespDto.of(ResultType.SUCCESS, "계정 로그아웃 완료", null)
        );
    }
}