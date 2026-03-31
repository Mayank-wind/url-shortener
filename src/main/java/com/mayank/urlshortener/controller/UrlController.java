package com.mayank.urlshortener.controller;

import com.mayank.urlshortener.dto.ShortenUrlRequest;
import com.mayank.urlshortener.dto.ShortenUrlResponse;
import com.mayank.urlshortener.dto.UrlStatsResponse;
import com.mayank.urlshortener.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
public class UrlController {

    @Autowired
    private UrlService service;

    @PostMapping("/shorten")
    public ShortenUrlResponse shorten(@RequestBody ShortenUrlRequest request) {
        return service.shortenUrl(request);
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl) {
        String originalUrl = service.getOriginalUrl(shortUrl);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
    @GetMapping("/stats/{shortUrl}")
    public UrlStatsResponse getStats(@PathVariable String shortUrl) {
        return service.getStats(shortUrl);
    }
}