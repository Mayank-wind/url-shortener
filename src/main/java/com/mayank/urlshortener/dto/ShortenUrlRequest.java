package com.mayank.urlshortener.dto;

import lombok.Data;

@Data
public class ShortenUrlRequest {
    private String url;
    private String customAlias;

}
