package xyz.samsami.sentinel_server.account.mapper;

import xyz.samsami.sentinel_server.account.domain.Account;
import xyz.samsami.sentinel_server.account.dto.AccountRespCreateDto;

public class AccountMapper {
    public static AccountRespCreateDto toRespDto(Account account) {
        return AccountRespCreateDto.builder()
            .accountId(account.getId())
            .build();
    }
}