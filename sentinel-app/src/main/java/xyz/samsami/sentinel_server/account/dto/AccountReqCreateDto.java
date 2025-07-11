package xyz.samsami.sentinel_server.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.samsami.sentinel_server.account.type.AccountType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountReqCreateDto {
    private String email;
    private String password;
    private AccountType type;

    private String nickname;
    private String bio;
}