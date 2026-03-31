package com.mayank.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShortenUrlResponse {
    private String shortUrl;
    private String originalUrl;
}
