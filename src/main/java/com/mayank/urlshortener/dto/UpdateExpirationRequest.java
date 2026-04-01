package com.mayank.urlshortener.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateExpirationRequest {
    private LocalDateTime expiresAt;
}
