package com.mayank.urlshortener.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShortenUrlRequest {
    private String url;
    private String customAlias;
    private LocalDateTime expiresAt;

}
