package xyz.samsami.sentinel_server.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRespLoginDto {
    private String accessToken;
    private String refreshToken;
}
