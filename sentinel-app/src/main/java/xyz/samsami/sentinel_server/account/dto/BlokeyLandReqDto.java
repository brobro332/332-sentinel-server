package xyz.samsami.sentinel_server.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlokeyLandReqDto {
    private UUID id;
    private String nickname;
    private String bio;
}
