package com.mayank.urlshortener.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String shortUrl;
    private String originalUrl;
    private LocalDateTime createdAt;
    private Long clickCount =0L;
    private LocalDateTime expiresAt;
    private LocalDateTime lastAccessedAt;

}