package xyz.samsami.sentinel_server.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountType {
    BLOKEY_LAND("프로젝트 관리 서비스");

    private final String description;
}
