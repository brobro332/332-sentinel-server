package xyz.samsami.sentinel_server.common.exception;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record CommonExceptionEntity(String code, String message, OffsetDateTime timestamp) { }